package me.lotabout.codegenerator.action;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;

public class CodeGeneratorAction extends BaseGenerateAction {
    private String templateKey;

    public CodeGeneratorAction(String templateKey, String templateName) {
        super(new CodeGeneratorActionHandler(templateKey));
        getTemplatePresentation().setDescription("description");
        getTemplatePresentation().setText(templateName, false);
    }
}
