package me.lotabout.codegenerator.util;

import org.jetbrains.annotations.Nullable;
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
     * Check if the member has the annotation with the fully qualified name.
     * <p>
     * This method will also check the simple name of the annotation.
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
