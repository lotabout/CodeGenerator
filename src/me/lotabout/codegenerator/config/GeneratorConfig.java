package me.lotabout.codegenerator.config;

import org.jetbrains.java.generate.config.DuplicationPolicy;
import org.jetbrains.java.generate.config.InsertWhere;

public class GeneratorConfig {
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

    public String template = DEFAULT_TEMPLATE;
    public String fileEncoding = DEFAULT_ENCODING;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneratorConfig that = (GeneratorConfig) o;

        if (useFullyQualifiedName != that.useFullyQualifiedName) return false;
        if (filterConstantField != that.filterConstantField) return false;
        if (filterEnumField != that.filterEnumField) return false;
        if (filterTransientModifier != that.filterTransientModifier) return false;
        if (filterStaticModifier != that.filterStaticModifier) return false;
        if (filterLoggers != that.filterLoggers) return false;
        if (enableMethods != that.enableMethods) return false;
        if (jumpToMethod != that.jumpToMethod) return false;
        if (sortElements != that.sortElements) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (fileExtension != null ? !fileExtension.equals(that.fileExtension) : that.fileExtension != null)
            return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (insertNewMethodOption != that.insertNewMethodOption) return false;
        if (whenDuplicatesOption != that.whenDuplicatesOption) return false;
        if (filterFieldName != null ? !filterFieldName.equals(that.filterFieldName) : that.filterFieldName != null)
            return false;
        if (filterMethodName != null ? !filterMethodName.equals(that.filterMethodName) : that.filterMethodName != null)
            return false;
        if (filterMethodType != null ? !filterMethodType.equals(that.filterMethodType) : that.filterMethodType != null)
            return false;
        if (filterFieldType != null ? !filterFieldType.equals(that.filterFieldType) : that.filterFieldType != null)
            return false;
        if (template != null ? !template.equals(that.template) : that.template != null) return false;
        return fileEncoding != null ? fileEncoding.equals(that.fileEncoding) : that.fileEncoding == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (fileExtension != null ? fileExtension.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (useFullyQualifiedName ? 1 : 0);
        result = 31 * result + (insertNewMethodOption != null ? insertNewMethodOption.hashCode() : 0);
        result = 31 * result + (whenDuplicatesOption != null ? whenDuplicatesOption.hashCode() : 0);
        result = 31 * result + (filterConstantField ? 1 : 0);
        result = 31 * result + (filterEnumField ? 1 : 0);
        result = 31 * result + (filterTransientModifier ? 1 : 0);
        result = 31 * result + (filterStaticModifier ? 1 : 0);
        result = 31 * result + (filterLoggers ? 1 : 0);
        result = 31 * result + (filterFieldName != null ? filterFieldName.hashCode() : 0);
        result = 31 * result + (filterMethodName != null ? filterMethodName.hashCode() : 0);
        result = 31 * result + (filterMethodType != null ? filterMethodType.hashCode() : 0);
        result = 31 * result + (filterFieldType != null ? filterFieldType.hashCode() : 0);
        result = 31 * result + (enableMethods ? 1 : 0);
        result = 31 * result + (jumpToMethod ? 1 : 0);
        result = 31 * result + sortElements;
        result = 31 * result + (template != null ? template.hashCode() : 0);
        result = 31 * result + (fileEncoding != null ? fileEncoding.hashCode() : 0);
        return result;
    }
}
