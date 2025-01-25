package me.lotabout.codegenerator.action;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.generate.config.Config;
import org.jetbrains.java.generate.config.FilterPattern;
import org.jetbrains.java.generate.exception.GenerateCodeException;

import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.codeInsight.generation.PsiFieldMember;
import com.intellij.codeInsight.generation.PsiMethodMember;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.util.MemberChooser;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
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

import static me.lotabout.codegenerator.util.GenerationUtil.convertClassMembersToPsiMembers;
import static me.lotabout.codegenerator.util.GenerationUtil.velocityEvaluate;

public class CodeGeneratorAction extends AnAction {
    private static final Logger logger = Logger.getInstance(CodeGeneratorAction.class);
    private final String templateKey;
    private final CodeGeneratorSettings settings;

    public CodeGeneratorAction(final String templateKey, final String templateName) {
        getTemplatePresentation().setDescription("description");
        getTemplatePresentation().setText(templateName, false);

        this.settings = ApplicationManager.getApplication().getService(CodeGeneratorSettings.class);;
        this.templateKey = templateKey;
    }

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return super.getActionUpdateThread();
    }

    @Override
    public void update(final AnActionEvent e) {
        // Code Generation action could run without editor
        final Presentation presentation = e.getPresentation();
        final Project project = e.getProject();
        if (project == null) {
            presentation.setEnabled(false);
        }

        // final PsiFile file = e.getDataContext().getData(LangDataKeys.PSI_FILE);
        // if (!(file instanceof PsiJavaFile)) {
        //     presentation.setEnabled(false);
        // }

        presentation.setEnabled(true);
    }

    @Override
    public void actionPerformed(final AnActionEvent e) {
        final CodeTemplate codeTemplate = settings
            .getCodeTemplate(templateKey)
            .orElseThrow(IllegalStateException::new);
        final Project project = e.getProject();
        if (project == null) {
            logger.info("No project found in context");
            return;
        }
        final DataContext context = e.getDataContext();
        final PsiFile file = context.getData(LangDataKeys.PSI_FILE);
        if (file == null) {
            logger.info("No file found in context");
            return;
        }
        if ((! (file instanceof PsiJavaFile)) && !codeTemplate.type.isSupportNonJavaFile()) {
            logger.info("Not a java file, cannot execute template: " + codeTemplate.name);
            return;
        }
        final Editor editor = context.getData(LangDataKeys.EDITOR);
        if (editor == null) {
            if (codeTemplate.type.isNeedEditor()) {
                logger.info("No editor found in context");
                return;
            }
        }
        final Map<String, Object> contextMap = executePipeline(codeTemplate, file, editor);
        if (contextMap == null) {
            return;     // early return from pipeline
        }

        switch (codeTemplate.type) {
            case CLASS:
                assert (file instanceof PsiJavaFile);
                JavaClassWorker.execute(codeTemplate, settings.getIncludes(), (PsiJavaFile) file, contextMap);
                break;
            case BODY:
                assert (editor != null);
                final PsiClass clazz = getSubjectClass(editor, file);
                if (clazz == null) {
                    HintManager.getInstance().showErrorHint(editor, "no parent class found for current cursor position");
                    return;
                }
                JavaBodyWorker.execute(codeTemplate, settings.getIncludes(), clazz, editor, contextMap);
                break;
            case CARET:
                assert (editor != null);
                JavaCaretWorker.execute(codeTemplate, settings.getIncludes(), file, editor, contextMap);
                break;
            default:
                throw new IllegalStateException("template type is not recognized: " + codeTemplate.type);
        }
    }

    private Map<String, Object> executePipeline(@NotNull final CodeTemplate codeTemplate,
        @NotNull final PsiFile file, final Editor editor) {
        final Project project = file.getProject();
        logger.debug("+++ executePipeline - START +++");
        if (logger.isDebugEnabled()) {
            logger.debug("Current project " + project.getName());
        }

        final Map<String, Object> contextMap = new HashMap<>();
        PsiClass clazz = getSubjectClass(editor, file);
        if (clazz == null) {
            clazz = buildFakeClassForEmptyFile(file);
        }
        contextMap.put("class0", EntryFactory.of(clazz));

        if (editor != null) {
            final int offset = editor.getCaretModel().getOffset();
            final PsiElement context = file.findElementAt(offset);
            final PsiMethod parentMethod = PsiTreeUtil.getParentOfType(context, PsiMethod.class, false);
            contextMap.put("parentMethod", EntryFactory.of(parentMethod));
        }

        logger.debug("Select member/class through pipeline");
        for (final PipelineStep step : codeTemplate.pipeline) {
            if (!step.enabled()) continue;
            switch (step.type()) {
                case "class-selection":
                    final PsiClass selectedClass = selectClass(file, (ClassSelectionConfig) step, contextMap);
                    if (selectedClass == null) return null;
                    contextMap.put("class" + step.postfix(), EntryFactory.of(selectedClass));
                    break;
                case "member-selection":
                    final List<PsiMember> selectedMembers = selectMember(file, (MemberSelectionConfig) step, contextMap);
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
    private static PsiClass getSubjectClass(final Editor editor, @NotNull final PsiFile file) {
        if (editor != null) {
            final int offset = editor.getCaretModel().getOffset();
            final PsiElement context = file.findElementAt(offset);
            if (context == null) {
                return null;
            }
            return PsiTreeUtil.getParentOfType(context, PsiClass.class, false);
        } else {
            return getFirstClass(file);
        }
    }

    private PsiClass selectClass(@NotNull final PsiFile file,
            final ClassSelectionConfig config, final Map<String, Object> contextMap) {
        final String initialClassNameTemplate = config.initialClass;
        final Project project = file.getProject();
        try {
            final String className;
            if (StringUtils.isEmpty(initialClassNameTemplate)) {
                className = null;
            } else {
                className = velocityEvaluate(project, contextMap,
                    contextMap, initialClassNameTemplate, settings.getIncludes());
            }
            logger.debug("Initial class name for class selection is ", className);
            PsiClass initialClass = null;
            if (!StringUtils.isEmpty(className)) {
                initialClass = JavaPsiFacade
                    .getInstance(project)
                    .findClass(className, GlobalSearchScope.allScope(project));
            }

            if (initialClass == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("could not found initialClass" + className);
                }
                initialClass = getFirstClass(file);
            }

            final TreeClassChooser chooser = TreeClassChooserFactory
                .getInstance(project)
                .createProjectScopeChooser("Select a class", initialClass);
            chooser.showDialog();

            if (chooser.getSelected() == null) {
                return null;
            }
            return chooser.getSelected();
        } catch (final GenerateCodeException e) {
            Messages.showMessageDialog(project, e.getMessage(), "Generate Failed", null);
        }
        return null;
    }

    private List<PsiMember> selectMember(@NotNull final PsiFile file,
            final MemberSelectionConfig config, final Map<String, Object> contextMap) {
        final String AVAILABLE_MEMBERS = "availableMembers";
        final String SELECTED_MEMBERS = "selectedMembers";
        final Project project = file.getProject();

        logger.debug("start to select members by template: ", config.providerTemplate);
        velocityEvaluate(project, contextMap, contextMap, config.providerTemplate,settings.getIncludes());

        // members should be MemberEntry[] or PsiMember[]
        List<?> availableMembers = Collections.emptyList();
        List<?> selectedMembers = Collections.emptyList();
        if (contextMap.containsKey(AVAILABLE_MEMBERS)) {
            availableMembers = (List<?>) contextMap.get(AVAILABLE_MEMBERS);
            selectedMembers = (List<?>) contextMap.get(SELECTED_MEMBERS);
            selectedMembers = (selectedMembers == null ? availableMembers : selectedMembers);
        }

        contextMap.remove(AVAILABLE_MEMBERS);
        contextMap.remove(SELECTED_MEMBERS);

        // filter the members by configuration
        final PsiElementClassMember<?>[] dialogMembers = buildClassMember(filterMembers(availableMembers, config));
        final PsiElementClassMember<?>[] membersSelected = buildClassMember(filterMembers(selectedMembers, config));

        if (!config.allowEmptySelection && dialogMembers.length <= 0) {
            Messages.showMessageDialog(project,
                "No members are provided to select from.\nAnd template doesn't allow empty selection",
                "Warning", Messages.getWarningIcon());
            return null;
        }

        final MemberChooser<PsiElementClassMember<?>> chooser =
                new MemberChooser<>(dialogMembers, config.allowEmptySelection,
                        config.allowMultiSelection, project,
                        PsiUtil.isLanguageLevel5OrHigher(file),
                        new JPanel(new BorderLayout())) {
                    @NotNull
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
        return convertClassMembersToPsiMembers(chooser.getSelectedElements());
    }

    private static List<PsiMember> filterMembers(final List<?> members,
            final MemberSelectionConfig config) {
        final FilterPattern pattern = generatorConfig2Config(config).getFilterPattern();
        return members.stream()
                .map(member -> {
                    if (member instanceof PsiMember) {
                        return (PsiMember) member;
                    } else if (member instanceof MemberEntry) {
                        return (((MemberEntry<?>) member).getRaw());
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

    private static PsiElementClassMember<?>[] buildClassMember(final List<PsiMember> members) {
      return members
          .stream()
          .filter(m -> (m instanceof PsiField) || (m instanceof PsiMethod))
          .map(m -> {
              if (m instanceof PsiField) {
                  return new PsiFieldMember((PsiField) m);
              } else {
                  return new PsiMethodMember((PsiMethod) m);
              }
          }).toArray(PsiElementClassMember[]::new);
    }

    private static Config generatorConfig2Config(final MemberSelectionConfig selectionConfig) {
        final Config config = new Config();
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

    private static PsiClass getFirstClass(final PsiFile file) {
        if (file instanceof PsiJavaFile) {
            final PsiClass[] classes = ((PsiJavaFile) file).getClasses();
            return (classes.length > 0 ? classes[0] : null);
        } else {
            return null;
        }
    }

    private static PsiClass buildFakeClassForEmptyFile(@NotNull final PsiFile file) {
        final Project project = file.getProject();
        final VirtualFile moduleRoot = ProjectRootManager
            .getInstance(project)
            .getFileIndex()
            .getSourceRootForFile(file.getVirtualFile());
        if (moduleRoot == null) {
            return null;
        }
        final String fileName = file.getName();
        final String className = fileName.replace(".java", "");
        final String packageName = file
            .getVirtualFile()
            .getPath()
            .substring(moduleRoot.getPath().length() + 1)
            .replace(File.separator + fileName, "")
            .replace(File.separator, ".");

        try {
            final PsiFile element = PsiFileFactory
                .getInstance(project)
                .createFileFromText("filename", JavaFileType.INSTANCE,
                    "package " + packageName + ";\n" + "class " + className + "{}");
            return (PsiClass) element.getLastChild();
        } catch (final IncorrectOperationException ignore) {
        }
        return null;
    }
}
