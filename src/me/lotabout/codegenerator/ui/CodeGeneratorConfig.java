package me.lotabout.codegenerator.ui;

import com.intellij.ui.components.JBTabbedPane;
import me.lotabout.codegenerator.CodeGeneratorSettings;
import me.lotabout.codegenerator.CodeTemplate;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CodeGeneratorConfig {
    private JPanel mainPane;
    private JButton addTemplateButton;
    private JBTabbedPane tabbedPane;
    private Map<String, TemplateEditPane> editPaneMap;

    public CodeGeneratorConfig(CodeGeneratorSettings settings) {
        tabbedPane = new JBTabbedPane();
        editPaneMap = new HashMap<>();

        addTemplateButton.addActionListener(e -> {
            TemplateEditPane editPane = new TemplateEditPane(settings, "", this);
            String title = "Untitled";
            tabbedPane.addTab(title, editPane.templateEdit());
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
            editPaneMap.put(title, editPane);
        });

        resetTabPane(settings);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.weighty = 1;
        mainPane.add(tabbedPane, constraints);
    }

    public void refresh(CodeGeneratorSettings settings) {
        tabbedPane.removeAll();
        editPaneMap.clear();
        resetTabPane(settings);
    }


    private void resetTabPane(CodeGeneratorSettings settings) {
        settings.getCodeTemplates().forEach((key, value) -> {
            TemplateEditPane editPane = new TemplateEditPane(settings, key, this);
            tabbedPane.addTab(key, editPane.templateEdit());
            editPaneMap.put(key, editPane);
        });
    }

    public Map<String, CodeTemplate> getTabTemplates() {
        Map<String, CodeTemplate> map = new HashMap<>();
        editPaneMap.forEach((key, value) -> {
            CodeTemplate codeTemplate = new CodeTemplate()
                    .setName(value.templateName())
                    .setType(value.templateType())
                    .setTemplate(value.template())
                    .setFileEncoding(value.fileEncoding());
            map.put(codeTemplate.name(), codeTemplate);
        });
        return map;
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
