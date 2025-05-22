package me.lotabout.codegenerator.ui;

import javax.swing.JComponent;

import me.lotabout.codegenerator.config.PipelineStep;

public interface PipelineStepConfig {
    PipelineStep getConfig();
    JComponent getComponent();
}
