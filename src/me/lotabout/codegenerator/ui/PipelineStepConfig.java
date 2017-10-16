package me.lotabout.codegenerator.ui;

import me.lotabout.codegenerator.config.PipelineStep;

import javax.swing.*;

public interface PipelineStepConfig {
    int step();
    PipelineStep getConfig();
    JComponent getComponent();
}
