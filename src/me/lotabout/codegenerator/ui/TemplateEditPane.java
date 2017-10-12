package me.lotabout.codegenerator.ui;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import me.lotabout.codegenerator.CodeGeneratorSettings;
import me.lotabout.codegenerator.CodeTemplate;

import javax.swing.*;
import java.awt.*;

public class TemplateEditPane {
    private JPanel     templateEdit;
    private JComboBox  templateTypeCombo;
    private JTextField templateNameText;
    private JButton    deleteTemplateButton;
    private JPanel     editorPane;
    private JTextField fileEncodingText;
    private Editor editor;

    public TemplateEditPane(CodeGeneratorSettings settings, String templateName,
            CodeGeneratorConfig parentPane) {
        CodeTemplate template = settings.getCodeTemplate(templateName).orElseGet(CodeTemplate::new);

        templateNameText.setText(template.name());
        fileEncodingText.setText(StringUtil.notNullize(template.fileEncoding(), CodeTemplate.DEFAULT_ENCODING));
        templateTypeCombo.setSelectedItem(template.type());

        addVmEditor(template.template());
        deleteTemplateButton.addActionListener(e -> {
            int result = Messages.showYesNoDialog("Delete this template?", "Delete", null);
            if (result == Messages.OK) {
                settings.removeCodeTemplate(templateName);
                parentPane.refresh(settings);
            }
        });
    }

    private void addVmEditor(String template) {
        EditorFactory factory = EditorFactory.getInstance();
        Document velocityTemplate = factory.createDocument(template);
        editor = factory.createEditor(velocityTemplate, null, FileTypeManager.getInstance()
                .getFileTypeByExtension("vm"), false);
        GridConstraints constraints = new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(300, 300), null, 0, true);
        editorPane.add(editor.getComponent(), constraints);
    }

    public String templateName() {
        return templateNameText.getText();
    }

    public String template() {
        return editor.getDocument().getText();
    }

    public String fileEncoding() {
        return fileEncodingText.getText();
    }

    public String templateType() {
        return (String) templateTypeCombo.getSelectedItem();
    }

    public JPanel templateEdit() {
        return templateEdit;
    }
}
