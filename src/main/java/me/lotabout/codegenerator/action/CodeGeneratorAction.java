package me.lotabout.codegenerator.action;

import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.codeInsight.generation.PsiFieldMember;
import com.intellij.codeInsight.generation.PsiMethodMember;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.util.MemberChooser;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.IncorrectOperationException;
import me.lotabout.codegenerator.CodeGeneratorSettings;
import me.lotabout.codegenerator.config.ClassSelectionConfig;
import me.lotabout.codegenerator.config.CodeTemplate;
import me.lotabout.codegenerator.config.MemberSelectionConfig;
import me.lotabout.codegenerator.config.PipelineStep;
import me.lotabout.codegenerator.util.EntryFactory;
import me.lotabout.codegenerator.util.GenerationUtil;
import me.lotabout.codegenerator.util.MemberEntry;
import me.lotabout.codegenerator.worker.JavaBodyWorker;
import me.lotabout.codegenerator.worker.JavaCaretWorker;
import me.lotabout.codegenerator.worker.JavaClassWorker;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.generate.config.Config;
import org.jetbrains.java.generate.config.FilterPattern;
import org.jetbrains.java.generate.exception.GenerateCodeException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CodeGeneratorAction extends AnAction {
    private static final Logger logger = Logger.getInstance(CodeGeneratorAction.class);
    private final String templateKey;
    private final CodeGeneratorSettings settings;

    public CodeGeneratorAction(String templateKey, String templateName) {
        getTemplatePresentation().setDescription("description");
        getTemplatePresentation().setText(templateName, false);

        this.settings = ServiceManager.getService(CodeGeneratorSettings.class);;
        this.templateKey = templateKey;
    }

    @Override
    public boolean startInTransaction() {
        return true;
    }

    @Override
    public void update(AnActionEvent e) {
        // Code Generation action could run without editor
        Presentation presentation = e.getPresentation();
        Project project = e.getProject();
        if (project == null) {
            presentation.setEnabled(false);
        }

        PsiFile file = e.getDataContext().getData(DataKeys.PSI_FILE);
        if (file == null || !(file instanceof PsiJavaFile)) {
            presentation.setEnabled(false);
        }

        presentation.setEnabled(true);
    }

    @Override public void actionPerformed(AnActionEvent e) {
        final CodeTemplate codeTemplate = settings.getCodeTemplate(templateKey).orElseThrow(IllegalStateException::new);
        Project project = e.getProject();
        assert project != null;

        PsiFile file = e.getDataContext().getData(DataKeys.PSI_FILE);
        assert file != null && file instanceof PsiJavaFile;

        PsiJavaFile javaFile = (PsiJavaFile)file;

        Editor editor = e.getDataContext().getData(DataKeys.EDITOR);

        Map<String, Object> contextMap = executePipeline(codeTemplate, javaFile, editor);
        if (contextMap == null) {
            // early return from pipeline
            return;
        }

        switch (codeTemplate.type) {
            case "class":
                JavaClassWorker.execute(codeTemplate, settings.getIncludes(), javaFile, contextMap);
                break;
            case "body":
                assert editor != null;
                PsiClass clazz = getSubjectClass(editor, javaFile);
                if (clazz == null) {
                    HintManager.getInstance().showErrorHint(editor, "no parent class found for current cursor position");
                    return;
                }

                JavaBodyWorker.execute(codeTemplate, settings.getIncludes(), clazz, editor, contextMap);
                break;
            case "caret":
                assert editor != null;
                JavaCaretWorker.execute(codeTemplate, settings.getIncludes(), javaFile, editor, contextMap);
                break;
            default:
                throw new IllegalStateException("template type is not recognized: " + codeTemplate.type);
        }
    }

    private Map<String, Object> executePipeline(@NotNull CodeTemplate codeTemplate, @NotNull final PsiJavaFile file, final Editor editor) {
        final Project project = file.getProject();
        logger.debug("+++ executePipeline - START +++");
        if (logger.isDebugEnabled()) {
            logger.debug("Current project " + project.getName());
        }

        Map<String, Object> contextMap = new HashMap<>();
        PsiClass clazz = getSubjectClass(editor, file);
        if (clazz == null) {
            clazz = buildFakeClassForEmptyFile(file);
        }
        contextMap.put("class0", EntryFactory.of(clazz));

        if (editor != null) {
            int offset = editor.getCaretModel().getOffset();
            PsiElement context = file.findElementAt(offset);
            PsiMethod parentMethod = PsiTreeUtil.getParentOfType(context, PsiMethod.class, false);
            contextMap.put("parentMethod", EntryFactory.of(parentMethod));
        }

        logger.debug("Select member/class through pipeline");
        for (PipelineStep step : codeTemplate.pipeline) {
            if (!step.enabled()) continue;
            switch (step.type()) {
                case "class-selection":
                    PsiClass selectedClass = selectClass(file, (ClassSelectionConfig) step, contextMap);
                    if (selectedClass == null) return null;
                    contextMap.put("class" + step.postfix(), EntryFactory.of(selectedClass));
                    break;
                case "member-selection":
                    List<PsiMember> selectedMembers = selectMember(file, (MemberSelectionConfig) step, contextMap);
                    if (selectedMembers == null) return null;
                    GenerationUtil.insertMembersToContext(selectedMembers,
                            Collections.emptyList(),
                            contextMap,
                            step.postfix(),
                            ((MemberSelectionConfig) step).sortElements);
                    break;
                default:
                    throw new IllegalStateException("step type not recognized: " + step.type());
            }
        }

        return contextMap;
    }

    @Nullable
    private static PsiClass getSubjectClass(Editor editor, @NotNull final PsiJavaFile file) {
        PsiClass clazz = null;
        if (editor != null) {
            int offset = editor.getCaretModel().getOffset();
            PsiElement context = file.findElementAt(offset);
            if (context == null)
                return null;

            clazz = PsiTreeUtil.getParentOfType(context, PsiClass.class, false);
        } else if (file.getClasses().length > 0) {
            clazz = file.getClasses()[0];
        }

        return clazz;
    }

    private PsiClass selectClass(@NotNull PsiJavaFile file, ClassSelectionConfig config, Map<String, Object> contextMap) {
        String initialClassNameTemplate = config.initialClass;
        Project project = file.getProject();
        try {
            String className = GenerationUtil.velocityEvaluate(project, contextMap, contextMap, initialClassNameTemplate, settings.getIncludes());
            if (logger.isDebugEnabled()) logger.debug("Initial class name for class selection is" + className);

            PsiClass initialClass = null;
            if (!StringUtils.isEmpty(className)) {
                initialClass = JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.allScope(project));
            }

            if (initialClass == null) {
                if (logger.isDebugEnabled()) logger.debug("could not found initialClass" + className);
                initialClass = file.getClasses().length > 0 ? file.getClasses()[0] : null;
            }

            TreeClassChooser chooser = TreeClassChooserFactory.getInstance(project)
                    .createProjectScopeChooser("Select a class", initialClass);
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

    private List<PsiMember> selectMember(@NotNull PsiJavaFile file, MemberSelectionConfig config, Map<String, Object> contextMap) {
        final String AVAILABLE_MEMBERS = "availableMembers";
        final String SELECTED_MEMBERS = "selectedMembers";
        final Project project = file.getProject();

        if (logger.isDebugEnabled()) logger.debug("start to select members by template: ", config.providerTemplate);
        GenerationUtil.velocityEvaluate(project, contextMap, contextMap, config.providerTemplate,settings.getIncludes());

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
                new MemberChooser<PsiElementClassMember>(dialogMembers, config.allowEmptySelection, config.allowMultiSelection, project, PsiUtil.isLanguageLevel5OrHigher(file), new JPanel(new BorderLayout())) {
                    @Nullable
                    @Override
                    protected String getHelpId() {
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
                        return !pattern.fieldMatches((PsiField) member);
                    }
                    if (config.enableMethods && member instanceof PsiMethod) {
                        return !pattern.methodMatches((PsiMethod) member);
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
                        return new PsiFieldMember((PsiField) m);
                    } else if (m instanceof PsiMethod) {
                        return new PsiMethodMember((PsiMethod) m);
                    } else {
                        return null;
                    }
                }).collect(Collectors.toList());

        return ret.toArray(new PsiElementClassMember[ret.size()]);
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

    private static PsiClass buildFakeClassForEmptyFile(@NotNull PsiJavaFile file) {
        final Project project = file.getProject();
        final VirtualFile moduleRoot = ProjectRootManager.getInstance(project).getFileIndex().getSourceRootForFile(file.getVirtualFile());
        final String fileName = file.getName();
        final String className = fileName.replace(".java", "");
        final String packageName = file.getVirtualFile().getPath()
                .substring(moduleRoot.getPath().length() + 1)
                .replace(File.separator + fileName, "")
                .replaceAll(File.separator, ".");

        try {
            final PsiFile element = PsiFileFactory.getInstance(project)
                    .createFileFromText("filename", JavaFileType.INSTANCE,
                            "package " + packageName + ";\n" +
                                    "class " + className + "{}");
            return (PsiClass) element.getLastChild();
        } catch (IncorrectOperationException ignore) {
        }
        return null;
    }
}
