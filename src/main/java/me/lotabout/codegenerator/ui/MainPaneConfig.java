package me.lotabout.codegenerator.ui;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import me.lotabout.codegenerator.ui.include.IncludeConfig;

public class MainPaneConfig {

    private JPanel mainPanel;
    private JTabbedPane tabbedPane;

    public MainPaneConfig(final CodeGeneratorConfig codeGeneratorConfig,
            final IncludeConfig includeConfig) {
        tabbedPane.add("Code Templates", codeGeneratorConfig.getMainPane());
        tabbedPane.add("Includes", includeConfig.getMainPane());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
