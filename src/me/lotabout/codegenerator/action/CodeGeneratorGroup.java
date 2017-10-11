package me.lotabout.codegenerator.action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import me.lotabout.codegenerator.CodeGeneratorSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class CodeGeneratorGroup extends ActionGroup implements DumbAware {
    private CodeGeneratorSettings settings;

    public CodeGeneratorGroup() {
        settings = ServiceManager.getService(CodeGeneratorSettings.class);
    }

    @NotNull @Override public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
        if (anActionEvent == null) {
            return AnAction.EMPTY_ARRAY;
        }

        Project project = PlatformDataKeys.PROJECT.getData(anActionEvent.getDataContext());
        if (project == null) {
            return AnAction.EMPTY_ARRAY;
        }

        final List<AnAction> children = settings.getCodeTemplates()
                .keySet().stream()
                .map(CodeGeneratorGroup::getOrCreateAction)
                .collect(Collectors.toList());

        return children.toArray(new AnAction[children.size()]);
    }

    private static AnAction getOrCreateAction(String templateName) {
        final String actionId = "CodeMaker.Menu.Action." + templateName;
        AnAction action = ActionManager.getInstance().getAction(actionId);
        if (action == null) {
            action = new CodeGeneratorAction(templateName);
            ActionManager.getInstance().registerAction(actionId, action);
        }
        return action;
    }
}
