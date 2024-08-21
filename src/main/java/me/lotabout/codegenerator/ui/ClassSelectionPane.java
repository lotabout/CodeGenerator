package me.lotabout.codegenerator.ui;

import javax.swing.JPanel;
import javax.swing.JTextField;

import me.lotabout.codegenerator.config.ClassSelectionConfig;

public class ClassSelectionPane implements PipelineStepConfig {
    private JPanel topPane;
    private JTextField initialClassText;

    public ClassSelectionPane(final ClassSelectionConfig config) {
        initialClassText.setText(config.initialClass);
    }

    @Override
    public ClassSelectionConfig getConfig() {
        final ClassSelectionConfig config = new ClassSelectionConfig();
        config.initialClass = initialClassText.getText();
        return config;
    }

    @Override
    public JPanel getComponent() {
        return topPane;
    }
}
