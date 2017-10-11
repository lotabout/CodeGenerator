package me.lotabout.codegenerator.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import me.lotabout.codegenerator.CodeGeneratorSettings;

public class CodeGeneratorAction extends AnAction implements DumbAware {

    private static final Logger log = Logger.getInstance(CodeGeneratorAction.class);

    private CodeGeneratorSettings settings;

    private String templateKey;

    CodeGeneratorAction(String templateKey) {
        this.settings = ServiceManager.getService(CodeGeneratorSettings.class);;
        this.templateKey = templateKey;
        getTemplatePresentation().setDescription("description");
        getTemplatePresentation().setText(templateKey, false);
    }

    @Override public void actionPerformed(AnActionEvent anActionEvent) {

    }
}
