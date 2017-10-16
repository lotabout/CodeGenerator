package me.lotabout.codegenerator.config;

public class MemberSelectionConfig implements PipelineStep {
    public boolean filterConstantField = true;
    public boolean filterEnumField = false;
    public boolean filterTransientModifier = false;
    public boolean filterStaticModifier = true;
    public boolean filterLoggers = true;
    public String filterFieldName = "";
    public String filterFieldType = "";
    public String filterMethodName = "";
    public String filterMethodType = "";
    public boolean enableMethods = false;
    public String providerTemplate = "$class0.members";

    @Override public String type() {
        return "member-selection";
    }
}
