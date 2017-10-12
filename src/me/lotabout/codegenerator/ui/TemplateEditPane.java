package me.lotabout.codegenerator.ui;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import me.lotabout.codegenerator.CodeGeneratorSettings;
import me.lotabout.codegenerator.config.GeneratorConfig;
import org.jetbrains.java.generate.config.DuplicationPolicy;
import org.jetbrains.java.generate.config.InsertWhere;

import javax.swing.*;
import java.awt.*;

public class TemplateEditPane {
    private JPanel templateEdit;
    private JComboBox templateTypeCombo;
    private JTextField templateNameText;
    private JButton deleteTemplateButton;
    private JPanel editorPane;
    private JTextField fileEncodingText;
    private JTabbedPane tabbedPane1;
    private JCheckBox fullQualifiedCheckBox;
    private JCheckBox enableMethodsCheckBox;
    private JCheckBox jumpToMethodCheckBox;
    private JCheckBox sortElementsCheckBox;
    private JComboBox comboBoxSortElements;
    private JCheckBox excludeConstantCheckBox;
    private JCheckBox excludeStaticCheckBox;
    private JCheckBox excludeTransientCheckBox;
    private JCheckBox excludeEnumCheckBox;
    private JCheckBox excludeLogerCheckBox;
    private JTextField textExcludeFieldsByName;
    private JTextField textExcludeFieldsByType;
    private JTextField textExcludeMethodsByName;
    private JTextField textExcludeMethodsByType;
    private JRadioButton askRadioButton;
    private JRadioButton replaceExistingRadioButton;
    private JRadioButton generateDuplicateMemberRadioButton;
    private JRadioButton atCaretRadioButton;
    private JRadioButton atEndOfClassRadioButton;
    private Editor editor;

    public TemplateEditPane(CodeGeneratorSettings settings, String templateName,
                            CodeGeneratorConfig parentPane) {
        GeneratorConfig generatorConfig = settings.getCodeTemplate(templateName).orElseGet(GeneratorConfig::new);

        templateNameText.setText(generatorConfig.name);
        fileEncodingText.setText(StringUtil.notNullize(generatorConfig.fileEncoding, GeneratorConfig.DEFAULT_ENCODING));
        templateTypeCombo.setSelectedItem(generatorConfig.type);
        fullQualifiedCheckBox.setSelected(generatorConfig.useFullyQualifiedName);
        enableMethodsCheckBox.setSelected(generatorConfig.enableMethods);
        jumpToMethodCheckBox.setSelected(generatorConfig.jumpToMethod);
        sortElementsCheckBox.setSelected(generatorConfig.sortElements != 0);
        comboBoxSortElements.setSelectedIndex(generatorConfig.sortElements - 1);
        excludeConstantCheckBox.setSelected(generatorConfig.filterConstantField);
        excludeStaticCheckBox.setSelected(generatorConfig.filterStaticModifier);
        excludeTransientCheckBox.setSelected(generatorConfig.filterTransientModifier);
        excludeEnumCheckBox.setSelected(generatorConfig.filterEnumField);
        excludeLogerCheckBox.setSelected(generatorConfig.filterLoggers);
        textExcludeFieldsByName.setText(generatorConfig.filterFieldName);
        textExcludeFieldsByType.setText(generatorConfig.filterFieldType);
        textExcludeMethodsByName.setText(generatorConfig.filterMethodName);
        textExcludeMethodsByType.setText(generatorConfig.filterMethodType);

        askRadioButton.setSelected(false);
        replaceExistingRadioButton.setSelected(false);
        generateDuplicateMemberRadioButton.setSelected(false);
        switch (generatorConfig.whenDuplicatesOption) {
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
        switch (generatorConfig.insertNewMethodOption) {
            case AT_CARET:
                atCaretRadioButton.setSelected(true);
                break;
            case AT_THE_END_OF_A_CLASS:
                atEndOfClassRadioButton.setSelected(true);
                break;
            default:
                break;
        }

        addVmEditor(generatorConfig.template);
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

    public String name() {
        return templateNameText.getText();
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
        return excludeLogerCheckBox.isSelected();
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
}
