package me.lotabout.codegenerator.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.generate.element.FieldElement;

import com.intellij.psi.PsiField;

/**
 * Wrapper around FieldElement
 */
public class FieldEntry implements MemberEntry<PsiField> {
    private final PsiField raw;
    private final FieldElement element;

    public FieldEntry(final PsiField field, final FieldElement element) {
        this.raw = field;
        this.element = element;
    }

    @Override
    public PsiField getRaw() {
        return raw;
    }

    public FieldElement getElement() {
        return element;
    }

    @Override
    public String getAccessor() {
        return element.getAccessor();
    }

    public boolean isConstant() {
        return element.isConstant();
    }

    public boolean isEnum() {
        return element.isEnum();
    }

    public boolean matchName(final String s) throws IllegalArgumentException {
        return element.matchName(s);
    }

    public String getName() {
        return element.getName();
    }

    public boolean isArray() {
        return element.isArray();
    }

    public boolean isNestedArray() {
        return element.isNestedArray();
    }

    public boolean isCollection() {
        return element.isCollection();
    }

    public boolean isMap() {
        return element.isMap();
    }

    public boolean isPrimitive() {
        return element.isPrimitive();
    }

    public boolean isPrimitiveArray() {
        return element.isPrimitiveArray();
    }

    public boolean isObject() {
        return element.isObject();
    }

    public boolean isObjectArray() {
        return element.isObjectArray();
    }

    public boolean isString() {
        return element.isString();
    }

    public boolean isStringArray() {
        return element.isStringArray();
    }

    public boolean isNumeric() {
        return element.isNumeric();
    }

    public boolean isDate() {
        return element.isDate();
    }

    public boolean isInstant() {
        final String type = element.getType();
        return type.equals("java.time.Instant");
    }

    public boolean isLocalDate() {
        final String type = element.getType();
        return type.equals("java.time.LocalDate");
    }

    public boolean isLocalTime() {
        final String type = element.getType();
        return type.equals("java.time.LocalTime");
    }

    public boolean isLocalDateTime() {
        final String type = element.getType();
        return type.equals("java.time.LocalDateTime");
    }

    public boolean isSet() {
        return element.isSet();
    }

    public boolean isList() {
        return element.isList();
    }

    public boolean isCalendar() {
        return element.isCalendar();
    }

    public String getTypeName() {
        return element.getTypeName().replace(">", "");
    }

    public String getTypeQualifiedName() {
        return element.getTypeQualifiedName();
    }

    public boolean isAnnotatedWith(@NotNull final String qualifiedName) {
        return AnnotationUtil.isAnnotatedWith(raw, qualifiedName);
    }

    public String getType() {
        return element.getType();
    }

    public boolean isBoolean() {
        return element.isBoolean();
    }

    public boolean isChar() {
        return element.isChar();
    }

    public boolean isByte() {
        return element.isByte();
    }

    public boolean isShort() {
        return element.isShort();
    }

    /**
     * Check if the field is an integer type.
     * <p>
     * This method will return {@code true} if the field is of type {@code int} or
     * {@code java.lang.Integer}.
     *
     * @return
     *     {@code true} if the field is an integer type, {@code false} otherwise.
     * @author Haixing Hu
     */
    public boolean isInt() {
        // NOTE: there is no Element.isInt() function
        final String type = element.getType();
        return type.equals("int") || type.equals("java.lang.Integer");
    }

    /**
     * Check if the field is a long type.
     * <p>
     * This method will return {@code true} if the field is of type {@code long} or
     * {@code java.lang.Long}.
     *
     * @return
     *     {@code true} if the field is a long type, {@code false} otherwise.
     * @author Haixing Hu
     */
    public boolean isLong() {
        // NOTE: Element.isLong() CANNOT detect Long object, which may be a bug
        final String type = element.getType();
        return type.equals("long") || type.equals("java.lang.Long");
    }

    public boolean isFloat() {
        return element.isFloat();
    }

    public boolean isDouble() {
        return element.isDouble();
    }

    public boolean isVoid() {
        return element.isVoid();
    }

    public boolean isNotNull() {
        return element.isNotNull();
    }

    public void setNotNull(final boolean b) {
        element.setNotNull(b);
    }

    public boolean isModifierStatic() {
        return element.isModifierStatic();
    }

    public boolean isModifierPublic() {
        return element.isModifierPublic();
    }

    public boolean isModifierProtected() {
        return element.isModifierProtected();
    }

    public boolean isModifierPackageLocal() {
        return element.isModifierPackageLocal();
    }

    public boolean isModifierPrivate() {
        return element.isModifierPrivate();
    }

    public boolean isModifierFinal() {
        return element.isModifierFinal();
    }

    public boolean isModifierTransient() {
        return element.isModifierTransient();
    }

    public boolean isModifierVolatile() {
        return element.isModifierVolatile();
    }


    @Override
    public String toString() {
        return "FieldEntry{" +
                "raw=" + raw +
                ", element=" + element +
                '}';
    }
}
