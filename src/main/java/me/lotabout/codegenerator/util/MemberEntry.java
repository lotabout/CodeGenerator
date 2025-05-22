package me.lotabout.codegenerator.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.generate.element.AbstractElement;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiArrayInitializerMemberValue;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMember;

/**
 * A generic interface representing a member (field or method) in a class.
 * Provides common functionality for accessing and manipulating member properties.
 *
 * @param <T> The specific type of PsiMember (PsiField or PsiMethod)
 * @author lotabout, Haixing Hu
 */
public interface MemberEntry<T extends PsiMember> {
    /**
     * Get the raw PSI member representation.
     *
     * @return The underlying PSI member object
     */
    T getRaw();

    /**
     * Get the AbstractElement wrapper for this member.
     *
     * @return The AbstractElement wrapper
     */
    AbstractElement getElement();

    /**
     * Get the name of this member.
     *
     * @return The member name
     */
    default String getName() {
        return getElement().getName();
    }

    /**
     * Get the accessor (getter/setter) name for this member.
     *
     * @return The accessor name
     */
    default String getAccessor() {
        return getElement().getAccessor();
    }

    /**
     * Check if this member's type is an array.
     *
     * @return true if array type, false otherwise
     */
    default boolean isArray() {
        return getElement().isArray();
    }

    /**
     * Check if this member's type is a nested array (array of arrays).
     *
     * @return true if nested array type, false otherwise
     */
    default boolean isNestedArray() {
        return getElement().isNestedArray();
    }

    /**
     * Check if this member's type is a primitive array.
     *
     * @return true if primitive array type, false otherwise
     */
    default boolean isPrimitiveArray() {
        return getElement().isPrimitiveArray();
    }

    /**
     * Check if this member's type is a String array.
     *
     * @return true if String array type, false otherwise
     */
    default boolean isStringArray() {
        return getElement().isStringArray();
    }

    /**
     * Check if this member's type is an Object array.
     *
     * @return true if Object array type, false otherwise
     */
    default boolean isObjectArray() {
        return getElement().isObjectArray();
    }

    /**
     * Check if this member's type is a Collection.
     *
     * @return true if Collection type, false otherwise
     */
    default boolean isCollection() {
        return getElement().isCollection();
    }

    /**
     * Check if this member's type is a List.
     *
     * @return true if List type, false otherwise
     */
    default boolean isList() {
        return getElement().isList();
    }

    /**
     * Check if this member's type is a Set.
     *
     * @return true if Set type, false otherwise
     */
    default boolean isSet() {
        return getElement().isSet();
    }

    /**
     * Check if this member's type is a Map.
     *
     * @return true if Map type, false otherwise
     */
    default boolean isMap() {
        return getElement().isMap();
    }

    /**
     * Check if this member's type is a primitive type.
     *
     * @return true if primitive type, false otherwise
     */
    default boolean isPrimitive() {
        return getElement().isPrimitive();
    }

    /**
     * Check if this member's type is a numeric type.
     *
     * @return true if numeric type, false otherwise
     */
    default boolean isNumeric() {
        return getElement().isNumeric();
    }

    /**
     * Check if this member's type is boolean.
     *
     * @return true if boolean type, false otherwise
     */
    default boolean isBoolean() {
        return getElement().isBoolean();
    }

    /**
     * Check if this member's type is char.
     *
     * @return true if char type, false otherwise
     */
    default boolean isChar() {
        return getElement().isChar();
    }

    /**
     * Check if this member's type is byte.
     *
     * @return true if byte type, false otherwise
     */
    default boolean isByte() {
        return getElement().isByte();
    }

    /**
     * Check if this member's type is short.
     *
     * @return true if short type, false otherwise
     */
    default boolean isShort() {
        return getElement().isShort();
    }

    /**
     * Check if this member's type is an integer type.
     * This method will return true if the type is either primitive int or java.lang.Integer.
     *
     * @return true if integer type, false otherwise
     * @author Haixing Hu
     */
    default boolean isInt() {
        final String type = getElement().getType();
        return type.equals("int") || type.equals("java.lang.Integer");
    }

    /**
     * Check if this member's type is a long type.
     * This method will return true if the type is either primitive long or java.lang.Long.
     *
     * @return true if long type, false otherwise
     * @author Haixing Hu
     */
    default boolean isLong() {
        final String type = getElement().getType();
        return type.equals("long") || type.equals("java.lang.Long");
    }

    /**
     * Check if this member's type is float.
     *
     * @return true if float type, false otherwise
     */
    default boolean isFloat() {
        return getElement().isFloat();
    }

    /**
     * Check if this member's type is double.
     *
     * @return true if double type, false otherwise
     */
    default boolean isDouble() {
        return getElement().isDouble();
    }

    /**
     * Check if this member's type is void.
     *
     * @return true if void type, false otherwise
     */
    default boolean isVoid() {
        return getElement().isVoid();
    }

    /**
     * Check if this member's type is String.
     *
     * @return true if String type, false otherwise
     */
    default boolean isString() {
        return getElement().isString();
    }

    /**
     * Check if this member's type is Object.
     *
     * @return true if Object type, false otherwise
     */
    default boolean isObject() {
        return getElement().isObject();
    }

    /**
     * Check if this member's type is java.util.Date.
     *
     * @return true if Date type, false otherwise
     */
    default boolean isDate() {
        return getElement().isDate();
    }

    /**
     * Check if this member's type is java.util.Calendar.
     *
     * @return true if Calendar type, false otherwise
     */
    default boolean isCalendar() {
        return getElement().isCalendar();
    }

    /**
     * Check if this member's type is java.time.Instant.
     *
     * @return true if Instant type, false otherwise
     */
    default boolean isInstant() {
        final String type = getElement().getType();
        return type.equals("java.time.Instant");
    }

    /**
     * Check if this member's type is java.time.LocalDate.
     *
     * @return true if LocalDate type, false otherwise
     */
    default boolean isLocalDate() {
        final String type = getElement().getType();
        return type.equals("java.time.LocalDate");
    }

    /**
     * Check if this member's type is java.time.LocalTime.
     *
     * @return true if LocalTime type, false otherwise
     */
    default boolean isLocalTime() {
        final String type = getElement().getType();
        return type.equals("java.time.LocalTime");
    }

    /**
     * Check if this member's type is java.time.LocalDateTime.
     *
     * @return true if LocalDateTime type, false otherwise
     */
    default boolean isLocalDateTime() {
        final String type = getElement().getType();
        return type.equals("java.time.LocalDateTime");
    }

    /**
     * Get the simple name of this member's type.
     *
     * @return The type name with any trailing '>' removed
     */
    default String getTypeName() {
        return getElement().getTypeName().replace(">", "");
    }

    /**
     * Get the fully qualified name of this member's type.
     *
     * @return The qualified type name
     */
    default String getTypeQualifiedName() {
        return getElement().getTypeQualifiedName();
    }

    /**
     * Check if this member is annotated with the specified annotation.
     *
     * @param qualifiedName The fully qualified name of the annotation
     * @return true if the member has the annotation, false otherwise
     */
    boolean isAnnotatedWith(@NotNull final String qualifiedName);

    /**
     * Get the type of this member as a ClassEntry.
     * For fields, this is the field type.
     * For methods, this is the return type.
     *
     * @return the type as a ClassEntry, or null if the type cannot be resolved
     */
    TypeEntry getType();

    /**
     * Check if this member is annotated with @NotNull or similar annotations.
     *
     * @return true if the member is marked as not null, false otherwise
     */
    default boolean isNotNull() {
        return getElement().isNotNull();
    }

    /**
     * Check if this member is static.
     *
     * @return true if static, false otherwise
     */
    default boolean isModifierStatic() {
        return getElement().isModifierStatic();
    }

    /**
     * Check if this member has public visibility.
     *
     * @return true if public, false otherwise
     */
    default boolean isModifierPublic() {
        return getElement().isModifierPublic();
    }

    /**
     * Check if this member has protected visibility.
     *
     * @return true if protected, false otherwise
     */
    default boolean isModifierProtected() {
        return getElement().isModifierProtected();
    }

    /**
     * Check if this member has package-private visibility.
     *
     * @return true if package-private, false otherwise
     */
    default boolean isModifierPackageLocal() {
        return getElement().isModifierPackageLocal();
    }

    /**
     * Check if this member has private visibility.
     *
     * @return true if private, false otherwise
     */
    default boolean isModifierPrivate() {
        return getElement().isModifierPrivate();
    }

    /**
     * Check if this member is final.
     *
     * @return true if final, false otherwise
     */
    default boolean isModifierFinal() {
        return getElement().isModifierFinal();
    }

    /**
     * Check if the member has the specified annotation.
     * This method will check both fully qualified name and simple name of the annotation.
     *
     * @param fullyQualifiedName The fully qualified name of the annotation
     * @return true if the member has the annotation, false otherwise
     * @author Haixing Hu
     */
    default boolean hasAnnotation(final String fullyQualifiedName) {
        final String simpleName = fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.') + 1);
        final T raw = getRaw();
        return raw.hasAnnotation(fullyQualifiedName) || raw.hasAnnotation(simpleName);
    }

    /**
     * Get the annotation with the specified name.
     * If the annotation with the fully qualified name does not exist,
     * this method will try to get the annotation with its simple name.
     *
     * @param fullyQualifiedName The fully qualified name of the annotation
     * @return The annotation if found, null otherwise
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
     * Get the value of an annotation attribute.
     * If the attribute does not exist, this method will try to get the attribute
     * using the simple name of the annotation.
     *
     * @param fullyQualifiedName The fully qualified name of the annotation
     * @param attributeName The name of the attribute
     * @return The attribute value as a String, or null if not found
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
     * Get the values of an annotation attribute.
     * If the attribute does not exist, this method will try to get the attribute
     * using the simple name of the annotation.
     * For single values, returns an array with one element.
     * For array values, returns an array with all elements.
     *
     * @param fullyQualifiedName The fully qualified name of the annotation
     * @param attributeName The name of the attribute
     * @return Array of attribute values as Strings, or null if not found
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
