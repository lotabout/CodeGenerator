package me.lotabout.codegenerator.action;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class CodeGeneratorAction extends CodeInsightAction {
    private String templateKey;
    private final CodeInsightActionHandler myHandler;

    public CodeGeneratorAction(String templateKey, String templateName) {
        getTemplatePresentation().setDescription("description");
        getTemplatePresentation().setText(templateName, false);
        this.myHandler = new CodeGeneratorActionHandler(templateKey);
    }

    @NotNull
    @Override
    protected CodeInsightActionHandler getHandler() {
        return myHandler;
    }


    @Override
    public void update(AnActionEvent e) {
        // Code Generation action could run without editor
        Presentation presentation = e.getPresentation();
        Project project = e.getProject();
        if (project == null) {
            presentation.setEnabled(false);
        }
    }
}
