package me.lotabout.codegenerator.ui;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import me.lotabout.codegenerator.config.ClassSelectionConfig;
import me.lotabout.codegenerator.config.CodeTemplate;
import me.lotabout.codegenerator.config.MemberSelectionConfig;
import org.jetbrains.java.generate.config.DuplicationPolicy;
import org.jetbrains.java.generate.config.InsertWhere;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
    private JRadioButton askRadioButton;
    private JRadioButton replaceExistingRadioButton;
    private JRadioButton generateDuplicateMemberRadioButton;
    private JRadioButton atCaretRadioButton;
    private JRadioButton atEndOfClassRadioButton;
    private JScrollPane settingsPanel;
    private JCheckBox templateEnabledCheckBox;
    private JTabbedPane templateTabbedPane;
    private JButton addMemberButton;
    private JButton addClassButton;
    private Editor editor;
    private java.util.List<PipelineStepConfig> pipeline = new ArrayList<>();

    public TemplateEditPane(CodeTemplate codeTemplate) {
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

        addMemberButton.addActionListener(e -> {
            MemberSelectionPane pane = new MemberSelectionPane(new MemberSelectionConfig(), this);
            pipeline.add(pane);
            templateTabbedPane.addTab("Member Selection", pane.getComponent());
            templateTabbedPane.setSelectedIndex(templateTabbedPane.getTabCount() - 1);
        });

        addClassButton.addActionListener(e -> {
            ClassSelectionPane pane = new ClassSelectionPane(new ClassSelectionConfig(), this);
            pipeline.add(pane);
            templateTabbedPane.addTab("Class Selection", pane.getComponent());
            templateTabbedPane.setSelectedIndex(templateTabbedPane.getTabCount() - 1);
        });

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

    public String toString() {
        return this.name();
    }

    public CodeTemplate getCodeTemplate() {
        CodeTemplate template = new CodeTemplate(this.id());
        template.name = this.name();
        template.type = this.type();
        template.enabled = this.enabled();
        template.fileEncoding = this.fileEncoding();
        template.template = this.template();
        template.enableMethods = this.enableMethods();
        template.jumpToMethod = this.jumpToMethod();
        template.sortElements = this.sortElements();
        template.insertNewMethodOption = this.insertWhere();
        template.whenDuplicatesOption = this.duplicationPolicy();
        template.pipeline = pipeline.stream().map(PipelineStepConfig::getConfig).collect(Collectors.toList());

        return template;
    }

    public void removePipelineStep(PipelineStepConfig stepToRemove) {
        int index = this.pipeline.indexOf(stepToRemove);
        PipelineStepConfig step = this.pipeline.remove(index);
        this.templateTabbedPane.remove(step.getComponent());
    }
}
