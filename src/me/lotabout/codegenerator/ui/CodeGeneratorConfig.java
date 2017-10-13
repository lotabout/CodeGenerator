package me.lotabout.codegenerator.ui;

import com.intellij.openapi.ui.Messages;
import me.lotabout.codegenerator.CodeGeneratorSettings;
import me.lotabout.codegenerator.config.CodeTemplate;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class CodeGeneratorConfig {
    private JPanel mainPane;
    private JButton addTemplateButton;
    private JSplitPane splitPane;
    private JList<TemplateEditPane> templateList;
    private DefaultListModel<TemplateEditPane> templateListModel;
    private JButton deleteTemplateButton;
    private JPanel splitRightPane;
    private JScrollPane scrollPane;

    public CodeGeneratorConfig(CodeGeneratorSettings settings) {
        this.templateListModel = new DefaultListModel<>();
        this.templateList.setModel(templateListModel);

        templateList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }

            int length = templateListModel.getSize();
            int index = templateList.getSelectedIndex();
            if (length < 0 || index < 0 || index >= length) {
                splitPane.setRightComponent(splitRightPane);
                deleteTemplateButton.setEnabled(false);
                return;
            }

            TemplateEditPane pane = templateListModel.get(templateList.getSelectedIndex());
            deleteTemplateButton.setEnabled(true);
            splitPane.setRightComponent(pane.templateEdit());
        });

        addTemplateButton.addActionListener(e -> {
            CodeTemplate template = new CodeTemplate();
            template.name = "Untitled";
            TemplateEditPane editPane = new TemplateEditPane(settings, template, this);
            DefaultListModel<TemplateEditPane> model = (DefaultListModel<TemplateEditPane>) templateList.getModel();
            model.addElement(editPane);
            templateList.setSelectedIndex(model.getSize()-1);
        });

        deleteTemplateButton.addActionListener(e -> {
            int index = templateList.getSelectedIndex();
            int size = templateListModel.getSize();
            if (index >= 0 && index < size) {
                int result = Messages.showYesNoDialog("Delete this template?", "Delete", null);
                if (result == Messages.OK) {
                    int lastIndex = templateList.getAnchorSelectionIndex();
                    templateListModel.remove(index);

                    int nextIndex = -1;
                    if (lastIndex >= 0 && lastIndex < index || lastIndex == index && index < size-1) {
                        nextIndex = lastIndex;
                    } else if (lastIndex == index || lastIndex > index && lastIndex < size-1) {
                        nextIndex = lastIndex - 1;
                    } else if (lastIndex >= index){
                        nextIndex = size-2; // should not be here
                    }
                    templateList.setSelectedIndex(nextIndex);
                }
            }
        });

        resetTabPane(settings);
    }

    public void refresh(CodeGeneratorSettings settings) {
        templateListModel.removeAllElements();
        resetTabPane(settings);
    }


    private void resetTabPane(CodeGeneratorSettings settings) {
        settings.getCodeTemplates().forEach((key, value) -> {
            TemplateEditPane editPane = new TemplateEditPane(settings, value, this);
            templateListModel.addElement(editPane);
        });

        // select first item
        templateList.setSelectedIndex(0);
    }

    public Map<String, CodeTemplate> getTabTemplates() {
        Map<String, CodeTemplate> ret = new HashMap<>();
        for (int i=0; i<templateListModel.getSize(); i++) {
            TemplateEditPane value = templateListModel.get(i);

            CodeTemplate codeTemplate = new CodeTemplate(value.id());
            codeTemplate.name = value.name();
            codeTemplate.type = value.type();
            codeTemplate.enabled = value.enabled();
            codeTemplate.fileEncoding = value.fileEncoding();
            codeTemplate.template = value.template();
            codeTemplate.useFullyQualifiedName = value.useFullyQualifiedName();
            codeTemplate.enableMethods = value.enableMethods();
            codeTemplate.jumpToMethod = value.jumpToMethod();
            codeTemplate.sortElements = value.sortElements();
            codeTemplate.filterConstantField = value.excludeConstant();
            codeTemplate.filterStaticModifier = value.excludeStatic();
            codeTemplate.filterTransientModifier = value.excludeTransient();
            codeTemplate.filterEnumField = value.excludeEnum();
            codeTemplate.filterLoggers = value.excludeLogger();
            codeTemplate.filterFieldName = value.excludeFieldsByName();
            codeTemplate.filterFieldType = value.excludeFieldsByType();
            codeTemplate.filterMethodName = value.excludeMethodsByName();
            codeTemplate.filterMethodType = value.excludeMethodsByType();
            codeTemplate.whenDuplicatesOption = value.duplicationPolicy();
            codeTemplate.insertNewMethodOption = value.insertWhere();
            codeTemplate.classNameVm = value.className();
            codeTemplate.classNumber = value.classNum();

            ret.put(codeTemplate.getId(), codeTemplate);
        }

        return ret;
    }

    /**
     * Getter method for property <tt>mainPane</tt>.
     *
     * @return property value of mainPane
     */
    public JPanel getMainPane() {
        return mainPane;
    }
}
