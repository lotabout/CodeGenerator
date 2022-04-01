package me.lotabout.codegenerator.ui.include;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.uiDesigner.core.GridConstraints;
import me.lotabout.codegenerator.config.include.Include;

import javax.swing.*;
import java.awt.*;

public class IncludeEditPane {
    private JPanel templateEdit;
    private JTextField templateIdText;
    private JTextField templateNameText;
    private JPanel editorPane;
    private JCheckBox defaultInclude;
    private Editor editor;

    public IncludeEditPane(Include include) {
        templateIdText.setText(include.getId());
        templateNameText.setText(include.getName());
        defaultInclude.setSelected(include.isDefaultInclude());
        addVmEditor(include.getContent());
    }


    private void addVmEditor(String template) {
        var factory = EditorFactory.getInstance();
        var velocityTemplate = factory.createDocument(template);
        editor = factory.createEditor(velocityTemplate, null, FileTypeManager.getInstance()
                .getFileTypeByExtension("vm"), false);
        var constraints = new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(0, 0), null, 0, true);

        editorPane.add(editor.getComponent(), constraints);
    }

    public String id() {
        return templateIdText.getText();
    }

    public String name() {
        return templateNameText.getText();
    }

    public String content() {
        return editor.getDocument().getText();
    }

    public JPanel templateEdit() {
        return templateEdit;
    }


    public String toString() {
        return this.name();
    }

    public Include getInclude() {
        var include = new Include(this.id());
        include.setName(this.name());
        include.setContent(this.content());
        include.setDefaultInclude(defaultInclude.isSelected());
        return include;
    }
}
