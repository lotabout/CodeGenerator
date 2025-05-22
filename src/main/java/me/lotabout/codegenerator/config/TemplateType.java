package me.lotabout.codegenerator.config;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * The enumeration of types of code templates.
 *
 * @author Haixing Hu
 */
@XmlEnum
public enum TemplateType {

  @XmlEnumValue("body")
  BODY("body", false, true),

  @XmlEnumValue("class")
  CLASS("class", false, false),

  @XmlEnumValue("caret")
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
