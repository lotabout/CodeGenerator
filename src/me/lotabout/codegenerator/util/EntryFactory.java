package me.lotabout.codegenerator.util;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import org.jetbrains.java.generate.element.ElementFactory;
import org.jetbrains.java.generate.element.FieldElement;
import org.jetbrains.java.generate.element.MethodElement;

public class EntryFactory {
    public static FieldEntry newFieldEntry(PsiField field, boolean useAccessor) {
        return new FieldEntry(field, ElementFactory.newFieldElement(field, useAccessor));
    }

    public static FieldEntry newFieldEntry(PsiClass clazz, FieldElement element) {
        PsiField field = clazz.findFieldByName(element.getName(), true);
        return new FieldEntry(field, element);
    }

    public static MethodEntry newMethodEntry(PsiMethod method) {
        return new MethodEntry(method, ElementFactory.newMethodElement(method));
    }

    public static MethodEntry newFieldEntry(PsiClass clazz, MethodElement element) {
//        PsiField field = clazz.findMethodsByName(element.getName(), true);
//        return new FieldEntry(field, element);
        return null;
    }

    public static ClassEntry newClassEntry(PsiClass clazz) {
        return ClassEntry.of(clazz, ElementFactory.newClassElement(clazz));
    }
}
