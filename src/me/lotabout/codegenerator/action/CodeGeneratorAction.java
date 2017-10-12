package me.lotabout.codegenerator.action;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import org.jetbrains.annotations.NotNull;

public class CodeGeneratorAction extends BaseGenerateAction {
    private String templateKey;

    public CodeGeneratorAction(String templateKey) {
        super(new CodeGeneratorActionHandler(templateKey));
        getTemplatePresentation().setDescription("description");
        getTemplatePresentation().setText(templateKey, false);
    }
}
