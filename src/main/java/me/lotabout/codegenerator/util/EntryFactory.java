package me.lotabout.codegenerator.util;

import org.jetbrains.java.generate.element.FieldElement;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;

public class EntryFactory {
    public static FieldEntry of(final PsiField field, final boolean useAccessor) {
        if (field == null) {
            return null;
        }
        return FieldEntry.of(field, useAccessor);
    }

    public static FieldEntry of(final PsiClass clazz, final FieldElement element) {
        if (clazz == null || element == null) {
            return null;
        }
        final PsiField field = clazz.findFieldByName(element.getName(), true);
        return field != null ? FieldEntry.of(field, false) : null;
    }

    public static MethodEntry of(final PsiMethod method) {
        if (method == null) {
            return null;
        }
        return MethodEntry.of(method);
    }
}
