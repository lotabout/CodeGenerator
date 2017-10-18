package me.lotabout.codegenerator.ui;

import me.lotabout.codegenerator.config.ClassSelectionConfig;

import javax.swing.*;

public class ClassSelectionPane implements PipelineStepConfig {
    private JPanel topPane;
    private JTextField initialClassText;

    public ClassSelectionPane(ClassSelectionConfig config) {
        initialClassText.setText(config.initialClass);
    }

    @Override
    public ClassSelectionConfig getConfig() {
        ClassSelectionConfig config = new ClassSelectionConfig();
        config.initialClass = initialClassText.getText();
        return config;
    }

    @Override
    public JPanel getComponent() {
        return topPane;
    }
}
