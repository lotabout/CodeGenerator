package me.lotabout.codegenerator.util;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import org.jetbrains.java.generate.element.ElementFactory;
import org.jetbrains.java.generate.element.FieldElement;

public class EntryFactory {
    public static FieldEntry of(PsiField field, boolean useAccessor) {
        if (field == null) return null;
        return new FieldEntry(field, ElementFactory.newFieldElement(field, useAccessor));
    }

    public static FieldEntry of(PsiClass clazz, FieldElement element) {
        if (clazz == null || element == null) return null;

        PsiField field = clazz.findFieldByName(element.getName(), true);
        return new FieldEntry(field, element);
    }

    public static MethodEntry of(PsiMethod method) {
        if (method == null) return null;
        return new MethodEntry(method, ElementFactory.newMethodElement(method));
    }

    public static ClassEntry of(PsiClass clazz) {
        if (clazz == null) return null;
        return ClassEntry.of(clazz, ElementFactory.newClassElement(clazz));
    }
}
