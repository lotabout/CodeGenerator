package me.lotabout.codegenerator.config;

/**
 * The enumeration of types of code templates.
 *
 * @author Haixing Hu
 */
public enum TemplateType {

  BODY("body", false, true),

  CLASS("class", false, false),

  CARET("caret", true, true);

  private final String value;

  private final boolean supportNonJavaFile;

  private final boolean needEditor;

  TemplateType(final String value, final boolean supportNonJavaFile, final boolean needEditor) {
    this.value = value;
    this.supportNonJavaFile = supportNonJavaFile;
    this.needEditor = needEditor;
  }

  public String getValue() {
    return value;
  }

  public boolean isSupportNonJavaFile() {
    return supportNonJavaFile;
  }

  public boolean isNeedEditor() {
    return needEditor;
  }

  public static TemplateType ofValue(final String value) {
    for (final TemplateType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid value: " + value);
  }
}
