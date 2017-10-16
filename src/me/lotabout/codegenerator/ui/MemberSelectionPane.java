package me.lotabout.codegenerator.ui;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.uiDesigner.core.GridConstraints;
import me.lotabout.codegenerator.config.MemberSelectionConfig;

import javax.swing.*;
import java.awt.*;

public class MemberSelectionPane implements PipelineStepConfig {
    private JPanel editorPane;
    private JCheckBox excludeConstantFieldsCheckBox;
    private JCheckBox excludeStaticFieldsCheckBox;
    private JCheckBox excludeTransientFieldsCheckBox;
    private JCheckBox excludeEnumFieldsCheckBox;
    private JCheckBox excludeLoggerFieldsLog4jCheckBox;
    private JTextField excludeFieldsNameText;
    private JTextField excludeFieldsByTypeText;
    private JTextField excludeMethodsByNameText;
    private JTextField excludeMethodsByTypeText;
    private JScrollPane scrollPanel;
    private JCheckBox enableMethodSelectionCheckBox;
    private JPanel topPane;
    private JButton removeStepButton;
    private Editor editor;

    public MemberSelectionPane(MemberSelectionConfig config, TemplateEditPane parent) {
        scrollPanel.getVerticalScrollBar().setUnitIncrement(16); // scroll speed

        excludeConstantFieldsCheckBox.setSelected(config.filterConstantField);
        excludeStaticFieldsCheckBox.setSelected(config.filterStaticModifier);
        excludeTransientFieldsCheckBox.setSelected(config.filterTransientModifier);
        excludeEnumFieldsCheckBox.setSelected(config.filterEnumField);
        excludeLoggerFieldsLog4jCheckBox.setSelected(config.filterLoggers);
        excludeFieldsNameText.setText(config.filterFieldName);
        excludeFieldsByTypeText.setText(config.filterFieldType);
        excludeMethodsByNameText.setText(config.filterMethodName);
        excludeMethodsByTypeText.setText(config.filterMethodType);
        enableMethodSelectionCheckBox.setSelected(config.enableMethods);
        addVmEditor(config.providerTemplate);

        removeStepButton.addActionListener(e -> {
            int result = Messages.showYesNoDialog("Really remove this step?", "Delete", null);
            if (result == Messages.OK) {
                parent.removePipelineStep(this);
            }
        });
    }

    @Override
    public MemberSelectionConfig getConfig() {
        MemberSelectionConfig config = new MemberSelectionConfig();
        config.filterConstantField = excludeConstantFieldsCheckBox.isSelected();
        config.filterEnumField = excludeEnumFieldsCheckBox.isSelected();
        config.filterTransientModifier = excludeTransientFieldsCheckBox.isSelected();
        config.filterStaticModifier = excludeStaticFieldsCheckBox.isSelected();
        config.filterLoggers = excludeLoggerFieldsLog4jCheckBox.isSelected();
        config.filterFieldName = excludeFieldsNameText.getText();
        config.filterFieldType = excludeFieldsByTypeText.getText();
        config.filterMethodName = excludeMethodsByNameText.getText();
        config.filterMethodType = excludeMethodsByTypeText.getText();
        config.enableMethods = enableMethodSelectionCheckBox.isSelected();
        config.providerTemplate = editor.getDocument().getText();
        return config;
    }

    private void addVmEditor(String template) {
        EditorFactory factory = EditorFactory.getInstance();
        Document velocityTemplate = factory.createDocument(template);
        editor = factory.createEditor(velocityTemplate, null, FileTypeManager.getInstance()
                .getFileTypeByExtension("vm"), false);
        GridConstraints constraints = new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(0, 0), null, 0, true);

        editorPane.add(editor.getComponent(), constraints);
    }

    @Override
    public JPanel getComponent() {
        return topPane;
    }
}
