package me.lotabout.codegenerator.action;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;

import me.lotabout.codegenerator.CodeGeneratorSettings;
import me.lotabout.codegenerator.config.CodeTemplate;

public class CodeGeneratorGroup extends ActionGroup implements DumbAware {

    private final CodeGeneratorSettings settings;

    public CodeGeneratorGroup() {
        settings = ApplicationManager.getApplication().getService(CodeGeneratorSettings.class);
    }

    @Override
    @NotNull
    public AnAction[] getChildren(@Nullable final AnActionEvent anActionEvent) {
        if (anActionEvent == null) {
            return EMPTY_ARRAY;
        }
        final DataContext context = anActionEvent.getDataContext();
        final Project project = PlatformDataKeys.PROJECT.getData(context);
        if (project == null) {
            return EMPTY_ARRAY;
        }
        final PsiFile file = context.getData(LangDataKeys.PSI_FILE);
        if (file == null) {
            return EMPTY_ARRAY;
        }
        final Caret caret = context.getData(LangDataKeys.CARET);
        final boolean isProjectView = (caret == null);

        final boolean isJavaFile = (file instanceof PsiJavaFile);
        final List<AnAction> children = settings
            .getCodeTemplates()
            .stream()
            .filter(t -> t.enabled)
            .filter(t -> !isProjectView || !t.type.isNeedEditor())
            .filter(t -> isJavaFile || t.type.isSupportNonJavaFile())
            // .filter(t -> file.getName().matches(t.fileNamePattern))
            .map(CodeGeneratorGroup::getOrCreateAction)
            .toList();
        return children.toArray(new AnAction[0]);
    }

    private static AnAction getOrCreateAction(final CodeTemplate template) {
        final String actionId = "CodeMaker.Menu.Action." + template.getId();
        AnAction action = ActionManager.getInstance().getAction(actionId);
        if (action == null) {
            action = new CodeGeneratorAction(template.getId(), template.name);
            ActionManager.getInstance().registerAction(actionId, action);
        }
        return action;
    }
}
