package me.lotabout.codegenerator.ui;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import me.lotabout.codegenerator.CodeGeneratorSettings;
import me.lotabout.codegenerator.config.CodeTemplate;
import org.jetbrains.java.generate.config.DuplicationPolicy;
import org.jetbrains.java.generate.config.InsertWhere;

import javax.swing.*;
import java.awt.*;

public class TemplateEditPane {
    private JPanel templateEdit;
    private JComboBox templateTypeCombo;
    private JTextField templateIdText;
    private JTextField templateNameText;
    private JPanel editorPane;
    private JTextField fileEncodingText;
    private JCheckBox fullQualifiedCheckBox;
    private JCheckBox enableMethodsCheckBox;
    private JCheckBox jumpToMethodCheckBox;
    private JCheckBox sortElementsCheckBox;
    private JComboBox comboBoxSortElements;
    private JCheckBox excludeConstantCheckBox;
    private JCheckBox excludeStaticCheckBox;
    private JCheckBox excludeTransientCheckBox;
    private JCheckBox excludeEnumCheckBox;
    private JCheckBox excludeLoggerCheckBox;
    private JTextField textExcludeFieldsByName;
    private JTextField textExcludeFieldsByType;
    private JTextField textExcludeMethodsByName;
    private JTextField textExcludeMethodsByType;
    private JRadioButton askRadioButton;
    private JRadioButton replaceExistingRadioButton;
    private JRadioButton generateDuplicateMemberRadioButton;
    private JRadioButton atCaretRadioButton;
    private JRadioButton atEndOfClassRadioButton;
    private JScrollPane settingsPanel;
    private JCheckBox templateEnabledCheckBox;
    private JTextField classNameText;
    private JSpinner classNumSpinner;
    private Editor editor;

    public TemplateEditPane(CodeGeneratorSettings settings, CodeTemplate codeTemplate,
                            CodeGeneratorConfig parentPane) {
        settingsPanel.getVerticalScrollBar().setUnitIncrement(16); // scroll speed

        templateIdText.setText(codeTemplate.getId());
        templateNameText.setText(codeTemplate.name);
        templateEnabledCheckBox.setSelected(codeTemplate.enabled);
        fileEncodingText.setText(StringUtil.notNullize(codeTemplate.fileEncoding, CodeTemplate.DEFAULT_ENCODING));
        templateTypeCombo.setSelectedItem(codeTemplate.type);
        fullQualifiedCheckBox.setSelected(codeTemplate.useFullyQualifiedName);
        enableMethodsCheckBox.setSelected(codeTemplate.enableMethods);
        jumpToMethodCheckBox.setSelected(codeTemplate.jumpToMethod);

        sortElementsCheckBox.addItemListener(e -> comboBoxSortElements.setEnabled(sortElementsCheckBox.isSelected()));
        comboBoxSortElements.setSelectedIndex(codeTemplate.sortElements - 1);
        sortElementsCheckBox.setSelected(codeTemplate.sortElements != 0);

        excludeConstantCheckBox.setSelected(codeTemplate.filterConstantField);
        excludeStaticCheckBox.setSelected(codeTemplate.filterStaticModifier);
        excludeTransientCheckBox.setSelected(codeTemplate.filterTransientModifier);
        excludeEnumCheckBox.setSelected(codeTemplate.filterEnumField);
        excludeLoggerCheckBox.setSelected(codeTemplate.filterLoggers);
        textExcludeFieldsByName.setText(codeTemplate.filterFieldName);
        textExcludeFieldsByType.setText(codeTemplate.filterFieldType);
        textExcludeMethodsByName.setText(codeTemplate.filterMethodName);
        textExcludeMethodsByType.setText(codeTemplate.filterMethodType);

        classNameText.setText(codeTemplate.classNameVm);
        classNumSpinner.setValue(codeTemplate.classNumber);

        askRadioButton.setSelected(false);
        replaceExistingRadioButton.setSelected(false);
        generateDuplicateMemberRadioButton.setSelected(false);
        switch (codeTemplate.whenDuplicatesOption) {
            case ASK:
                askRadioButton.setSelected(true);
                break;
            case REPLACE:
                replaceExistingRadioButton.setSelected(true);
                break;
            case DUPLICATE:
                generateDuplicateMemberRadioButton.setSelected(true);
                break;
        }

        atCaretRadioButton.setSelected(false);
        atEndOfClassRadioButton.setSelected(false);
        switch (codeTemplate.insertNewMethodOption) {
            case AT_CARET:
                atCaretRadioButton.setSelected(true);
                break;
            case AT_THE_END_OF_A_CLASS:
                atEndOfClassRadioButton.setSelected(true);
                break;
            default:
                break;
        }

        addVmEditor(codeTemplate.template);
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

    public String id() {
        return templateIdText.getText();
    }

    public String name() {
        return templateNameText.getText();
    }

    public boolean enabled() {
        return templateEnabledCheckBox.isSelected();
    }

    public String template() {
        return editor.getDocument().getText();
    }

    public String fileEncoding() {
        return fileEncodingText.getText();
    }

    public String type() {
        return (String) templateTypeCombo.getSelectedItem();
    }

    public boolean useFullyQualifiedName() {
        return fullQualifiedCheckBox.isSelected();
    }

    public boolean enableMethods() {
        return enableMethodsCheckBox.isSelected();
    }

    public boolean jumpToMethod() {
        return jumpToMethodCheckBox.isSelected();
    }

    public int sortElements() {
        if (!sortElementsCheckBox.isSelected()) {
            return 0;
        }
        return comboBoxSortElements.getSelectedIndex() + 1;
    }

    public boolean excludeConstant() {
        return excludeConstantCheckBox.isSelected();
    }

    public boolean excludeStatic() {
        return excludeStaticCheckBox.isSelected();
    }

    public boolean excludeTransient() {
        return excludeTransientCheckBox.isSelected();
    }

    public boolean excludeEnum() {
        return excludeEnumCheckBox.isSelected();
    }

    public boolean excludeLogger() {
        return excludeLoggerCheckBox.isSelected();
    }


    public String excludeFieldsByName() {
        return textExcludeFieldsByName.getText();
    }
    public String excludeFieldsByType() {
        return textExcludeFieldsByType.getText();
    }
    public String excludeMethodsByName() {
        return textExcludeMethodsByName.getText();
    }
    public String excludeMethodsByType() {
        return textExcludeMethodsByType.getText();
    }

    public DuplicationPolicy duplicationPolicy() {
        if (askRadioButton.isSelected()) {
            return DuplicationPolicy.ASK;
        } else if (replaceExistingRadioButton.isSelected()) {
            return DuplicationPolicy.REPLACE;
        } else if (generateDuplicateMemberRadioButton.isSelected()) {
            return DuplicationPolicy.DUPLICATE;
        }
        return DuplicationPolicy.ASK;
    }

    public InsertWhere insertWhere() {
        if (atCaretRadioButton.isSelected()) {
            return InsertWhere.AT_CARET;
        } else if (atEndOfClassRadioButton.isSelected()) {
            return InsertWhere.AT_THE_END_OF_A_CLASS;
        }
        return InsertWhere.AT_CARET;
    }

    public JPanel templateEdit() {
        return templateEdit;
    }

    public String className() {
        return classNameText.getText();
    }

    public int classNum() {
        return (int) classNumSpinner.getValue();
    }

    public String toString() {
        return this.name();
    }

}
