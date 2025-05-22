package me.lotabout.codegenerator.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "memberSelection")
@XmlAccessorType(XmlAccessType.FIELD)
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
    public String postfix = "";
    public boolean enabled = true;

    @Override public String type() {
        return "member-selection";
    }

    @Override
    public String postfix() {
        return postfix;
    }

    @Override
    public void postfix(final String postfix) {
        this.postfix = postfix;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public void enabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final MemberSelectionConfig that = (MemberSelectionConfig) o;

        if (filterConstantField != that.filterConstantField) return false;
        if (filterEnumField != that.filterEnumField) return false;
        if (filterTransientModifier != that.filterTransientModifier) return false;
        if (filterStaticModifier != that.filterStaticModifier) return false;
        if (filterLoggers != that.filterLoggers) return false;
        if (enableMethods != that.enableMethods) return false;
        if (allowMultiSelection != that.allowMultiSelection) return false;
        if (allowEmptySelection != that.allowEmptySelection) return false;
        if (sortElements != that.sortElements) return false;
        if (enabled != that.enabled) return false;
        if (filterFieldName != null ? !filterFieldName.equals(that.filterFieldName) : that.filterFieldName != null)
            return false;
        if (filterFieldType != null ? !filterFieldType.equals(that.filterFieldType) : that.filterFieldType != null)
            return false;
        if (filterMethodName != null ? !filterMethodName.equals(that.filterMethodName) : that.filterMethodName != null)
            return false;
        if (filterMethodType != null ? !filterMethodType.equals(that.filterMethodType) : that.filterMethodType != null)
            return false;
        if (providerTemplate != null ? !providerTemplate.equals(that.providerTemplate) : that.providerTemplate != null)
            return false;
        return postfix != null ? postfix.equals(that.postfix) : that.postfix == null;
    }

    @Override
    public int hashCode() {
        int result = (filterConstantField ? 1 : 0);
        result = 31 * result + (filterEnumField ? 1 : 0);
        result = 31 * result + (filterTransientModifier ? 1 : 0);
        result = 31 * result + (filterStaticModifier ? 1 : 0);
        result = 31 * result + (filterLoggers ? 1 : 0);
        result = 31 * result + (filterFieldName != null ? filterFieldName.hashCode() : 0);
        result = 31 * result + (filterFieldType != null ? filterFieldType.hashCode() : 0);
        result = 31 * result + (filterMethodName != null ? filterMethodName.hashCode() : 0);
        result = 31 * result + (filterMethodType != null ? filterMethodType.hashCode() : 0);
        result = 31 * result + (enableMethods ? 1 : 0);
        result = 31 * result + (providerTemplate != null ? providerTemplate.hashCode() : 0);
        result = 31 * result + (allowMultiSelection ? 1 : 0);
        result = 31 * result + (allowEmptySelection ? 1 : 0);
        result = 31 * result + sortElements;
        result = 31 * result + (postfix != null ? postfix.hashCode() : 0);
        result = 31 * result + (enabled ? 1 : 0);
        return result;
    }

    private static String DEFAULT_TEMPLATE = "## set `availableMembers` to provide the members to select\n"
            + "## set `selectedMembers` to select the members initially, set nothing to select all\n"
            + "## Note that it should be type List<PsiMember> or List<MemberEntry>\n"
            + "## And the selected result will be\n"
            + "## - fields1:  List<FieldEntry> where `1` is the step number that you specified\n"
            + "## - methods1: List<MethodEntry>\n"
            + "## - members:  List<MemberEntry>\n"
            + "#set($availableMembers = $class0.members)\n";

}
