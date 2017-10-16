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
import me.lotabout.codegenerator.config.MemberSelectionConfig;
import me.lotabout.codegenerator.config.PipelineStep;
import me.lotabout.codegenerator.util.EntryFactory;
import me.lotabout.codegenerator.config.CodeTemplate;
import me.lotabout.codegenerator.util.GenerationUtil;
import me.lotabout.codegenerator.worker.JavaWorker;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.generate.config.Config;
import org.jetbrains.java.generate.config.FilterPattern;
import org.jetbrains.java.generate.exception.GenerateCodeException;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

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
        contextMap.put("class0", EntryFactory.newClassEntry(clazz));

        logger.debug("Select member/class through pipeline");
        int numOfClass = 0;
        int numOfMembers = 0;

        for (PipelineStep step: template.pipeline) {
            switch (step.type()) {
            case "class-selection":
                numOfClass += 1;
                PsiClass selectedClass = selectClass(clazz, (ClassSelectionConfig)step, contextMap);
                if (selectedClass == null) return;
                contextMap.put("class"+numOfClass, selectedClass);
                break;
            case "member-selection":
                numOfMembers += 1;
                List<PsiMember> selectedMembers = selectMember(clazz, (MemberSelectionConfig)step, contextMap);
                if (selectedMembers == null) return;
                GenerationUtil.insertMembersToContext(selectedMembers, Collections.emptyList(), contextMap, numOfMembers, ((MemberSelectionConfig)step).sortElements);
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

        PsiMember[] availableMembers = new PsiMember[0];
        PsiMember[] selectedMembers = new PsiMember[0];
        if (contextMap.containsKey(AVAILABLE_MEMBERS)) {
            availableMembers = (PsiMember[])contextMap.get(AVAILABLE_MEMBERS);
            selectedMembers = (PsiMember[])contextMap.get(SELECTED_MEMBERS);
            selectedMembers = selectedMembers == null ? availableMembers : selectedMembers;
        }

        contextMap.remove(AVAILABLE_MEMBERS);
        contextMap.remove(SELECTED_MEMBERS);

        // filter the members by configuration
        FilterPattern filterPattern = generatorConfig2Config(config).getFilterPattern();
        PsiElementClassMember[] dialogMembers = buildClassMember(filterMembers(availableMembers, filterPattern));
        PsiElementClassMember[] membersSelected = buildClassMember(filterMembers(selectedMembers, filterPattern));

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

    private static PsiMember[] filterMembers(PsiMember[] members, FilterPattern pattern) {
        return (PsiMember[])Arrays.stream(members)
                .filter(member -> {
                    if (member instanceof PsiField) {
                        return pattern.fieldMatches((PsiField)member);
                    } else if (member instanceof PsiMethod) {
                        return pattern.methodMatches((PsiMethod)member);
                    } else {
                        return true;
                    }
                }).toArray();
    }

    private static PsiElementClassMember[] buildClassMember(PsiMember[] members) {
        return (PsiElementClassMember[])Arrays.stream(members)
                .filter(m -> (m instanceof PsiField) || (m instanceof PsiMethod))
                .map(m -> {
                    if (m instanceof PsiField) {
                        return new PsiFieldMember((PsiField)m);
                    } else if (m instanceof PsiMethod) {
                        return new PsiMethodMember((PsiMethod)m);
                    } else {
                        return null;
                    }
                }).toArray();
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
