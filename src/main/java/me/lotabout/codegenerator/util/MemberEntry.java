package me.lotabout.codegenerator.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.generate.element.AbstractElement;
import org.jetbrains.java.generate.element.Element;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiArrayInitializerMemberValue;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMember;

public interface MemberEntry<T extends PsiMember> extends Element {
    /**
     * Get the raw PsiMember
     *
     * @return
     *     the raw PsiMember
     */
    T getRaw();

    /**
     * Gets the underlying getElement().
     *
     * @return
     *     The underlying getElement().
     */
    AbstractElement getElement();

    @Override
    default String getName() {
        return getElement().getName();
    }

    @Override
    default String getAccessor() {
        return getElement().getAccessor();
    }

    @Override
    default boolean isArray() {
        return getElement().isArray();
    }

    @Override
    default boolean isNestedArray() {
        return getElement().isNestedArray();
    }

    @Override
    default boolean isPrimitiveArray() {
        return getElement().isPrimitiveArray();
    }

    @Override
    default boolean isStringArray() {
        return getElement().isStringArray();
    }

    @Override
    default boolean isObjectArray() {
        return getElement().isObjectArray();
    }

    @Override
    default boolean isCollection() {
        return getElement().isCollection();
    }

    @Override
    default boolean isList() {
        return getElement().isList();
    }

    @Override
    default boolean isSet() {
        return getElement().isSet();
    }

    @Override
    default boolean isMap() {
        return getElement().isMap();
    }

    @Override
    default boolean isPrimitive() {
        return getElement().isPrimitive();
    }

    @Override
    default boolean isNumeric() {
        return getElement().isNumeric();
    }

    @Override
    default boolean isBoolean() {
        return getElement().isBoolean();
    }

    @Override
    default boolean isChar() {
        return getElement().isChar();
    }

    @Override
    default boolean isByte() {
        return getElement().isByte();
    }

    @Override
    default boolean isShort() {
        return getElement().isShort();
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
    default boolean isInt() {
        // NOTE: there is no getElement().isInt() function
        final String type = getElement().getType();
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
    @Override
    default boolean isLong() {
        // NOTE: getElement().isLong() CANNOT detect Long object, which may be a bug
        final String type = getElement().getType();
        return type.equals("long") || type.equals("java.lang.Long");
    }

    @Override
    default boolean isFloat() {
        return getElement().isFloat();
    }

    @Override
    default boolean isDouble() {
        return getElement().isDouble();
    }

    @Override
    default boolean isVoid() {
        return getElement().isVoid();
    }

    @Override
    default boolean isString() {
        return getElement().isString();
    }

    @Override
    default boolean isObject() {
        return getElement().isObject();
    }

    @Override
    default boolean isDate() {
        return getElement().isDate();
    }

    @Override
    default boolean isCalendar() {
        return getElement().isCalendar();
    }

    default boolean isInstant() {
        final String type = getElement().getType();
        return type.equals("java.time.Instant");
    }

    default boolean isLocalDate() {
        final String type = getElement().getType();
        return type.equals("java.time.LocalDate");
    }

    default boolean isLocalTime() {
        final String type = getElement().getType();
        return type.equals("java.time.LocalTime");
    }

    default boolean isLocalDateTime() {
        final String type = getElement().getType();
        return type.equals("java.time.LocalDateTime");
    }

    @Override
    default String getTypeName() {
        return getElement().getTypeName().replace(">", "");
    }

    @Override
    default String getTypeQualifiedName() {
        return getElement().getTypeQualifiedName();
    }

    boolean isAnnotatedWith(@NotNull final String qualifiedName);

    @Override
    default String getType() {
        return getElement().getType();
    }

    @Override
    default boolean isNotNull() {
        return getElement().isNotNull();
    }

    void setNotNull(final boolean b);

    default boolean isModifierStatic() {
        return getElement().isModifierStatic();
    }

    default boolean isModifierPublic() {
        return getElement().isModifierPublic();
    }

    default boolean isModifierProtected() {
        return getElement().isModifierProtected();
    }

    default boolean isModifierPackageLocal() {
        return getElement().isModifierPackageLocal();
    }

    default boolean isModifierPrivate() {
        return getElement().isModifierPrivate();
    }

    default boolean isModifierFinal() {
        return getElement().isModifierFinal();
    }

    /**
     * Check if the member has the annotation with the fully qualified name.
     * <p>
     * If the member does has the annotation with the fully qualified name,
     * this method will also check the simple name of the annotation.
     *
     * @param fullyQualifiedName
     *     the fully qualified name of the annotation.
     * @return
     *     {@code true} if the member has the annotation, {@code false} otherwise.
     * @author Haixing Hu
     */
    default boolean hasAnnotation(final String fullyQualifiedName) {
        final String simpleName = fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.') + 1);
        final T raw = getRaw();
        return raw.hasAnnotation(fullyQualifiedName) || raw.hasAnnotation(simpleName);
    }

    /**
     * Get the annotation with the fully qualified name.
     * <p>
     * If the annotation with the fully qualified name does not exist, this method
     * will try to get the annotation with the simple name of the fully qualified name.
     *
     * @param fullyQualifiedName
     *     the fully qualified name of the annotation.
     * @return
     *     the annotation with the fully qualified name, or {@code null} if the
     *     annotation does not exist.
     * @author Haixing Hu
     */
    @Nullable
    default PsiAnnotation getAnnotation(final String fullyQualifiedName) {
        final T raw = getRaw();
        PsiAnnotation result = raw.getAnnotation(fullyQualifiedName);
        if (result == null) {
            final String simpleName = fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.') + 1);
            result = raw.getAnnotation(simpleName);
        }
        return result;
    }

    /**
     * Get the value of the attribute of the annotation with the fully qualified name.
     * <p>
     * If the attribute does not exist, this method will try to get the attribute of the
     * annotation with the simple name of the fully qualified name.
     *
     * @param fullyQualifiedName
     *     the fully qualified name of the annotation.
     * @param attributeName
     *     the name of the attribute.
     * @return
     *     the value of the attribute, or {@code null} if the attribute does not exist.
     */
    @Nullable
    default String getAnnotationAttribute(final String fullyQualifiedName, final String attributeName) {
        final PsiAnnotation annotation = getAnnotation(fullyQualifiedName);
        if (annotation != null) {
            final PsiAnnotationMemberValue value = annotation.findAttributeValue(attributeName);
            if (value != null) {
                return value.getText();
            }
        }
        return null;
    }

    /**
     * Get the values of the attribute of the annotation with the fully qualified name.
     * <p>
     * If the attribute does not exist, this method will try to get the attribute of the
     * annotation with the simple name of the fully qualified name.
     * <p>
     * If the attribute has a single value, this method will return an array with the
     * text representation of the single value; if the attribute has multiple values, this
     * method will return an array with the text representation of the multiple values.
     *
     * @param fullyQualifiedName
     *     the fully qualified name of the annotation.
     * @param attributeName
     *     the name of the attribute.
     * @return
     *     the values of the attribute, as an array; or {@code null} if the
     *     attribute does not exist.
     * @author Haixing Hu
     */
    @Nullable
    default String[] getAnnotationAttributes(final String fullyQualifiedName, final String attributeName) {
        final PsiAnnotation annotation = getAnnotation(fullyQualifiedName);
        if (annotation != null) {
            final PsiAnnotationMemberValue value = annotation.findAttributeValue(attributeName);
            if (value != null) {
                if (value instanceof PsiLiteralExpression) {
                    return new String[]{value.getText()};
                } else if (value instanceof PsiArrayInitializerMemberValue) {
                    return StringUtilEx.parseArrayInitializerText(value.getText());
                }
            }
        }
        return null;
    }
}
