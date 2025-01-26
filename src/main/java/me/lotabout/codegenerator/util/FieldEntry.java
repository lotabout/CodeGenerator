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

    @Override
    public FieldElement getElement() {
        return element;
    }

    boolean isConstant() {
        return element.isConstant();
    }

    public boolean isEnum() {
        return element.isEnum();
    }

    public boolean matchName(final String s) throws IllegalArgumentException {
        return element.matchName(s);
    }

    @Override
    public boolean isAnnotatedWith(@NotNull final String qualifiedName) {
        return AnnotationUtil.isAnnotatedWith(raw, qualifiedName);
    }

    @Override
    public void setNotNull(final boolean b) {
        element.setNotNull(b);
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
