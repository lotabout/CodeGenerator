package me.lotabout.codegenerator.action;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.codeInsight.generation.PsiFieldMember;
import com.intellij.codeInsight.generation.PsiMethodMember;
import com.intellij.ide.util.MemberChooser;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import me.lotabout.codegenerator.CodeGeneratorSettings;
import me.lotabout.codegenerator.config.ClassSelectionConfig;
import me.lotabout.codegenerator.config.CodeTemplate;
import me.lotabout.codegenerator.config.MemberSelectionConfig;
import me.lotabout.codegenerator.config.PipelineStep;
import me.lotabout.codegenerator.util.EntryFactory;
import me.lotabout.codegenerator.util.GenerationUtil;
import me.lotabout.codegenerator.util.MemberEntry;
import me.lotabout.codegenerator.worker.JavaWorker;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.generate.config.Config;
import org.jetbrains.java.generate.config.FilterPattern;
import org.jetbrains.java.generate.exception.GenerateCodeException;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CodeGeneratorActionHandler implements CodeInsightActionHandler {

    private static final Logger logger = Logger.getInstance(CodeGeneratorActionHandler.class);

    private CodeGeneratorSettings settings;

    private String templateId;

    CodeGeneratorActionHandler(String templateId) {
        this.settings = ServiceManager.getService(CodeGeneratorSettings.class);;
        this.templateId = templateId;
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile psiFile) {
        PsiClass clazz = getSubjectClass(editor, psiFile);
        assert clazz != null;

        if (!FileModificationService.getInstance().preparePsiElementsForWrite(clazz)) {
            return;
        }

        CodeTemplate codeTemplate = settings.getCodeTemplate(templateId).orElseThrow(IllegalStateException::new);
        if (psiFile instanceof PsiJavaFile) {
            executeOnJava(codeTemplate, project, clazz, editor);
        } else {
            return;
        }
    }

    private void executeOnJava(@NotNull final CodeTemplate template, @NotNull final Project project, @NotNull final PsiClass clazz, final Editor editor) {
        logger.debug("+++ executeOnJava - START +++");
        if (logger.isDebugEnabled()) {
            logger.debug("Current project " + project.getName());
        }

        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("class0", EntryFactory.of(clazz));

        logger.debug("Select member/class through pipeline");
        for (PipelineStep step: template.pipeline) {
            if (!step.enabled()) continue;
            switch (step.type()) {
            case "class-selection":
                PsiClass selectedClass = selectClass(clazz, (ClassSelectionConfig)step, contextMap);
                if (selectedClass == null) return;
                contextMap.put("class"+step.postfix(), EntryFactory.of(selectedClass));
                break;
            case "member-selection":
                List<PsiMember> selectedMembers = selectMember(clazz, (MemberSelectionConfig)step, contextMap);
                if (selectedMembers == null) return;
                GenerationUtil.insertMembersToContext(selectedMembers, Collections.emptyList(), contextMap, step.postfix(), ((MemberSelectionConfig)step).sortElements);
                break;
            }
        }

        // execute the template
        JavaWorker worker = new JavaWorker(clazz, editor, template);
        worker.execute(contextMap);
    }

    private PsiClass selectClass(@NotNull PsiClass clazz, ClassSelectionConfig config, Map<String, Object> contextMap) {
        String initialClassNameTemplate = config.initialClass;
        Project project = clazz.getProject();
        try {
            String className = GenerationUtil.velocityEvaluate(clazz, contextMap, contextMap, initialClassNameTemplate);
            if (logger.isDebugEnabled()) logger.debug("Initial class name for class selection is" + className);

            PsiClass initialClass = null;
            if (!StringUtils.isEmpty(className)) {
                initialClass = JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.allScope(project));
            }

            if (initialClass == null) {
                if (logger.isDebugEnabled()) logger.debug("could not found initialClass" + className);
                initialClass = clazz;
            }

            TreeClassChooser chooser = TreeClassChooserFactory.getInstance(project).createProjectScopeChooser("Select a class", initialClass);
            chooser.showDialog();

            if (chooser.getSelected() == null) {
                return null;
            }
            return chooser.getSelected();
        } catch (GenerateCodeException e) {
            Messages.showMessageDialog(project, e.getMessage(), "Generate Failed", null);
        }
        return null;
    }

    private List<PsiMember> selectMember(@NotNull PsiClass clazz, MemberSelectionConfig config, Map<String, Object> contextMap) {
        final String AVAILABLE_MEMBERS = "availableMembers";
        final String SELECTED_MEMBERS = "selectedMembers";
        final Project project = clazz.getProject();

        if (logger.isDebugEnabled()) logger.debug("start to select members by template: ", config.providerTemplate);
        GenerationUtil.velocityEvaluate(clazz, contextMap, contextMap, config.providerTemplate);

        // members should be MemberEntry[] or PsiMember[]
        List availableMembers = Collections.emptyList();
        List selectedMembers = Collections.emptyList();
        if (contextMap.containsKey(AVAILABLE_MEMBERS)) {
            availableMembers = (List) contextMap.get(AVAILABLE_MEMBERS);
            selectedMembers = (List) contextMap.get(SELECTED_MEMBERS);
            selectedMembers = selectedMembers == null ? availableMembers : selectedMembers;
        }

        contextMap.remove(AVAILABLE_MEMBERS);
        contextMap.remove(SELECTED_MEMBERS);

        // filter the members by configuration
        PsiElementClassMember[] dialogMembers = buildClassMember(filterMembers(availableMembers, config));
        PsiElementClassMember[] membersSelected = buildClassMember(filterMembers(selectedMembers, config));

        if (!config.allowEmptySelection && dialogMembers.length <= 0) {
            Messages.showMessageDialog(project, "No members are provided to select from.\nAnd template doesn't allow empty selection",
                    "Warning", Messages.getWarningIcon());
            return null;
        }

        final MemberChooser<PsiElementClassMember> chooser =
                new MemberChooser<PsiElementClassMember>(dialogMembers, config.allowEmptySelection, config.allowMultiSelection, project, PsiUtil.isLanguageLevel5OrHigher(clazz), new JPanel(new BorderLayout())) {
                    @Nullable @Override protected String getHelpId() {
                        return "editing.altInsert.codegenerator";
                    }
                };
        chooser.setTitle("Selection Fields for Code Generation");
        chooser.setCopyJavadocVisible(false);
        chooser.selectElements(membersSelected);
        chooser.show();

        if (DialogWrapper.OK_EXIT_CODE != chooser.getExitCode()) {
            return null; // indicate exit
        }

        return GenerationUtil.convertClassMembersToPsiMembers(chooser.getSelectedElements());
    }

    private static List<PsiMember> filterMembers(List<Object> members, final MemberSelectionConfig config) {
        FilterPattern pattern = generatorConfig2Config(config).getFilterPattern();
        return members.stream()
                .map(member -> {
                    if (member instanceof PsiMember) {
                        return (PsiMember) member;
                    } else if (member instanceof MemberEntry) {
                        return (((MemberEntry) member).getRaw());
                    } else {
                        return null;
                    }
                }).filter(member -> {
                    if (member instanceof PsiField) {
                        return !pattern.fieldMatches((PsiField)member);
                    } if (config.enableMethods && member instanceof PsiMethod) {
                        return !pattern.methodMatches((PsiMethod)member);
                    } else {
                        return false;
                    }
                }).collect(Collectors.toList());
    }

    private static PsiElementClassMember[] buildClassMember(List<PsiMember> members) {
        List<PsiElementClassMember> ret = members.stream()
                .filter(m -> (m instanceof PsiField) || (m instanceof PsiMethod))
                .map(m -> {
                    if (m instanceof PsiField) {
                        return new PsiFieldMember((PsiField)m);
                    } else if (m instanceof PsiMethod) {
                        return new PsiMethodMember((PsiMethod)m);
                    } else {
                        return null;
                    }
                }).collect(Collectors.toList());

        return ret.toArray(new PsiElementClassMember[ret.size()]);
    }

    @Nullable
    private static PsiClass getSubjectClass(Editor editor, final PsiFile file) {
        if (file == null) return null;

        int offset = editor.getCaretModel().getOffset();
        PsiElement context = file.findElementAt(offset);

        if (context == null) return null;

        PsiClass clazz = PsiTreeUtil.getParentOfType(context, PsiClass.class, false);
        if (clazz == null) {
            return null;
        }

        return clazz;
    }

    private static Config generatorConfig2Config(MemberSelectionConfig selectionConfig) {
        Config config = new Config();
        config.useFullyQualifiedName = false;
        config.filterConstantField = selectionConfig.filterConstantField;
        config.filterEnumField = selectionConfig.filterEnumField;
        config.filterTransientModifier = selectionConfig.filterTransientModifier;
        config.filterStaticModifier = selectionConfig.filterStaticModifier;
        config.filterFieldName = selectionConfig.filterFieldName;
        config.filterMethodName = selectionConfig.filterMethodName;
        config.filterMethodType = selectionConfig.filterMethodType;
        config.filterFieldType = selectionConfig.filterFieldType;
        config.filterLoggers = selectionConfig.filterLoggers;
        config.enableMethods = selectionConfig.enableMethods;
        return config;
    }

}
