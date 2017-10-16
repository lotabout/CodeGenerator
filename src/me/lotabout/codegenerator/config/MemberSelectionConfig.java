package me.lotabout.codegenerator.config;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

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
    public String providerTemplate = DEFAULT_TEMPLATE;
    public boolean allowMultiSelection = true;
    public boolean allowEmptySelection = true;
    public int sortElements = 0;

    @Override public String type() {
        return "member-selection";
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        MemberSelectionConfig that = (MemberSelectionConfig)o;

        return new EqualsBuilder()
                .append(filterConstantField, that.filterConstantField)
                .append(filterEnumField, that.filterEnumField)
                .append(filterTransientModifier, that.filterTransientModifier)
                .append(filterStaticModifier, that.filterStaticModifier)
                .append(filterLoggers, that.filterLoggers)
                .append(enableMethods, that.enableMethods)
                .append(allowMultiSelection, that.allowMultiSelection)
                .append(allowEmptySelection, that.allowEmptySelection)
                .append(sortElements, that.sortElements)
                .append(filterFieldName, that.filterFieldName)
                .append(filterFieldType, that.filterFieldType)
                .append(filterMethodName, that.filterMethodName)
                .append(filterMethodType, that.filterMethodType)
                .append(providerTemplate, that.providerTemplate)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(filterConstantField)
                .append(filterEnumField)
                .append(filterTransientModifier)
                .append(filterStaticModifier)
                .append(filterLoggers)
                .append(filterFieldName)
                .append(filterFieldType)
                .append(filterMethodName)
                .append(filterMethodType)
                .append(enableMethods)
                .append(providerTemplate)
                .append(allowMultiSelection)
                .append(allowEmptySelection)
                .append(sortElements)
                .toHashCode();
    }

    private static String DEFAULT_TEMPLATE = "## set `availableMembers` to provide the members to select\n"
            + "## set `selectedMembers` to select the members initially, set nothing to select all\n"
            + "#set($availableMembers = $class0.members)\n";

}
