package me.lotabout.codegenerator.config;

public interface PipelineStep {
    String type();
    String postfix();
    boolean enabled();
}
