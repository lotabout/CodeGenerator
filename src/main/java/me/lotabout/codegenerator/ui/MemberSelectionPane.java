package me.lotabout.codegenerator.ui;

import java.awt.Dimension;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.uiDesigner.core.GridConstraints;

import me.lotabout.codegenerator.config.MemberSelectionConfig;
import me.lotabout.codegenerator.config.PipelineStep;

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
    private JCheckBox enableMethodSelectionCheckBox;
    private JComboBox comboBoxSortElements;
    private JCheckBox sortElementsCheckBox;
    private JPanel topPane;
    private JCheckBox allowMultipleSelectionCheckBox;
    private JCheckBox allowEmptySelectionCheckBox;
    private Editor editor;

    MemberSelectionPane(final MemberSelectionConfig config) {
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
        sortElementsCheckBox.addItemListener(e -> comboBoxSortElements.setEnabled(sortElementsCheckBox.isSelected()));
        comboBoxSortElements.setSelectedIndex(config.sortElements - 1);
        sortElementsCheckBox.setSelected(config.sortElements != 0);
        allowEmptySelectionCheckBox.setSelected(config.allowEmptySelection);
        allowMultipleSelectionCheckBox.setSelected(config.allowMultiSelection);

        addVmEditor(config.providerTemplate);
    }

    private int sortElements() {
        if (!sortElementsCheckBox.isSelected()) {
            return 0;
        }
        return comboBoxSortElements.getSelectedIndex() + 1;
    }

    private void addVmEditor(final String template) {
        final EditorFactory factory = EditorFactory.getInstance();
        final Document velocityTemplate = factory.createDocument(template);
        editor = factory.createEditor(velocityTemplate, null, FileTypeManager.getInstance()
                .getFileTypeByExtension("vm"), false);
        final GridConstraints constraints = new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(0, 0), null, 0, true);

        editorPane.add(editor.getComponent(), constraints);
    }

    @Override
    public PipelineStep getConfig() {
        final MemberSelectionConfig config = new MemberSelectionConfig();
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
        config.allowEmptySelection = allowEmptySelectionCheckBox.isSelected();
        config.allowMultiSelection = allowMultipleSelectionCheckBox.isSelected();
        config.sortElements = sortElements();
        return config;
    }

    public JComponent getComponent() {
        return topPane;
    }
}
