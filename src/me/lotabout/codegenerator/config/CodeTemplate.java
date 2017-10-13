package me.lotabout.codegenerator.config;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jetbrains.java.generate.config.DuplicationPolicy;
import org.jetbrains.java.generate.config.InsertWhere;

import java.util.UUID;

public class CodeTemplate {
    private UUID id;
    public String name;
    public String fileExtension = ".java";
    public String type = "body";

    public boolean useFullyQualifiedName = false;
    public InsertWhere insertNewMethodOption = InsertWhere.AT_CARET;
    public DuplicationPolicy whenDuplicatesOption = DuplicationPolicy.ASK;
    public boolean filterConstantField = true;
    public boolean filterEnumField = false;
    public boolean filterTransientModifier = false;
    public boolean filterStaticModifier = true;
    public boolean filterLoggers = true;
    public String filterFieldName = "";
    public String filterMethodName = "";
    public String filterMethodType = "";
    public String filterFieldType = "";
    public boolean enableMethods = false;
    public boolean jumpToMethod = true; // jump cursor to toString method
    public int sortElements = 0; // 0 = none, 1 = asc, 2 = desc
    public boolean enabled = true;

    public String template = DEFAULT_TEMPLATE;
    public String fileEncoding = DEFAULT_ENCODING;

    public CodeTemplate(UUID id) {
        this.id = id;
    }
    public CodeTemplate(String id) {
        this.id = UUID.fromString(id);
    }

    public CodeTemplate() {
        this(UUID.randomUUID());
    }

    public String getId() {
        return this.id.toString();
    }

    public boolean isValid() {
        return true;
    }

    public static final String DEFAULT_ENCODING = "UTF-8";

    public static final String DEFAULT_TEMPLATE = ""
            + "## The available variables\n"
            + "## - List<FieldElement>  fields: The selected fields\n"
            + "## - List<MethodElement> methods: The selected methods (currently not supported)\n"
            + "## - List<Element>       members: selected (fields + methods)\n"
            + "## - ClassElement        class: The current class\n"
            + "## - String              classname: Class Name\n"
            + "## - String              FQClassname: Full Qualified Class Name\n"
            + "## - int                 java_version: java version\n"
            + "## - CodeStyleSettings   settings: settings of code style\n"
            + "## - Project             project: The project instance, normally used by Psi related utilities\n"
            + "## - GenerationHelper    helper:\n"
            + "## - StringUtil          StringUtil: The utility class to deal with string\n"
            + "## - NameUtil            NameUtil: The utility class to handle names\n"
            + "## - PsiShortNamesCache  PsiShortNamesCache: utility to search classes\n"
            + "## - PsiJavaPsiFacade    PsiJavaPsiFacade: Java specific utility to search classes\n"
            + "## - GlobalSearchScope   GlobalSearchScope: class to create search scopes, used by above utilities\n";

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        CodeTemplate template1 = (CodeTemplate)o;

        return new EqualsBuilder()
                .append(useFullyQualifiedName, template1.useFullyQualifiedName)
                .append(filterConstantField, template1.filterConstantField)
                .append(filterEnumField, template1.filterEnumField)
                .append(filterTransientModifier, template1.filterTransientModifier)
                .append(filterStaticModifier, template1.filterStaticModifier)
                .append(filterLoggers, template1.filterLoggers)
                .append(enableMethods, template1.enableMethods)
                .append(jumpToMethod, template1.jumpToMethod)
                .append(sortElements, template1.sortElements)
                .append(enabled, template1.enabled)
                .append(id, template1.id)
                .append(name, template1.name)
                .append(fileExtension, template1.fileExtension)
                .append(type, template1.type)
                .append(insertNewMethodOption, template1.insertNewMethodOption)
                .append(whenDuplicatesOption, template1.whenDuplicatesOption)
                .append(filterFieldName, template1.filterFieldName)
                .append(filterMethodName, template1.filterMethodName)
                .append(filterMethodType, template1.filterMethodType)
                .append(filterFieldType, template1.filterFieldType)
                .append(template, template1.template)
                .append(fileEncoding, template1.fileEncoding)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(fileExtension)
                .append(type)
                .append(useFullyQualifiedName)
                .append(insertNewMethodOption)
                .append(whenDuplicatesOption)
                .append(filterConstantField)
                .append(filterEnumField)
                .append(filterTransientModifier)
                .append(filterStaticModifier)
                .append(filterLoggers)
                .append(filterFieldName)
                .append(filterMethodName)
                .append(filterMethodType)
                .append(filterFieldType)
                .append(enableMethods)
                .append(jumpToMethod)
                .append(sortElements)
                .append(enabled)
                .append(template)
                .append(fileEncoding)
                .toHashCode();
    }
}
