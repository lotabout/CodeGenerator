package me.lotabout.codegenerator.config;

import com.intellij.openapi.util.text.StringUtil;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jetbrains.java.generate.config.DuplicationPolicy;
import org.jetbrains.java.generate.config.InsertWhere;

import java.util.UUID;

public class CodeTemplate {
    private UUID id;
    public String name = "Untitled";
    public String fileNamePattern = ".*\\.java$";
    public String type = "body";
    public boolean enabled = true;

    // used for body type template
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

    // used for class type template
    public int classNumber = 0;
    public String classNameVm = "$class0.name";

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
        switch (type) {
            case "body":
                return true;
            case "class":
                return StringUtil.isNotEmpty(classNameVm) && classNumber >= 0 && classNumber <= 10;
        }
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

}
