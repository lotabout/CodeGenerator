package me.lotabout.codegenerator.ui;

import me.lotabout.codegenerator.ui.include.IncludeConfig;

import javax.swing.*;

public class MainPaneConfig {

    private JPanel mainPanel;
    private JTabbedPane tabbedPane;

    public MainPaneConfig(CodeGeneratorConfig codeGeneratorConfig, IncludeConfig includeConfig) {
        tabbedPane.add("Code Templates", codeGeneratorConfig.getMainPane());
        tabbedPane.add("Includes", includeConfig.getMainPane());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
