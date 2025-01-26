package me.lotabout.codegenerator.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.generate.element.MethodElement;

import com.intellij.psi.PsiMethod;

/**
 * Wrapper around MethodElement
 */
public class MethodEntry implements MemberEntry<PsiMethod> {
    private final PsiMethod raw;
    private final MethodElement element;

    public MethodEntry(final PsiMethod field, final MethodElement element) {
        this.raw = field;
        this.element = element;
    }

    @Override
    public MethodElement getElement() {
        return element;
    }

    @Override
    public PsiMethod getRaw() {
        return raw;
    }

    public String getMethodName() {
        return element.getMethodName();
    }

    public void setMethodName(final String s) {
        element.setMethodName(s);
    }

    public String getFieldName() {
        return element.getFieldName();
    }

    public void setFieldName(final String s) {
        element.setFieldName(s);
    }

    public boolean isModifierAbstract() {
        return element.isModifierAbstract();
    }

    public void setModifierAbstract(final boolean b) {
        element.setModifierAbstract(b);
    }

    public boolean isModifierSynchronzied() {
        return element.isModifierSynchronzied();
    }

    public boolean isModifierSynchronized() {
        return element.isModifierSynchronized();
    }

    public void setModifierSynchronized(final boolean b) {
        element.setModifierSynchronized(b);
    }

    public boolean isReturnTypeVoid() {
        return element.isReturnTypeVoid();
    }

    public void setReturnTypeVoid(final boolean b) {
        element.setReturnTypeVoid(b);
    }

    public boolean isGetter() {
        return element.isGetter();
    }

    public void setGetter(final boolean b) {
        element.setGetter(b);
    }

    public boolean isDeprecated() {
        return element.isDeprecated();
    }

    public void setDeprecated(final boolean b) {
        element.setDeprecated(b);
    }

    @Override
    public boolean isAnnotatedWith(@NotNull final String qualifiedName) {
        return AnnotationUtil.isAnnotatedWith(raw, qualifiedName);
    }

    @Override
    public void setNotNull(final boolean b) {
        element.setNotNull(b);
    }

    @Override
    public String toString() {
        return "MethodEntry{" +
                "raw=" + raw +
                ", element=" + element +
                '}';
    }
}
