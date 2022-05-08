package me.lotabout.codegenerator.util;

import com.intellij.psi.PsiAnnotation;
import org.jetbrains.annotations.NotNull;

public class AnnotationEntry {
    private final String qualifiedName;
    private final PsiAnnotation psiAnnotation;

    public AnnotationEntry(@NotNull PsiAnnotation annotation) {
        this.psiAnnotation = annotation;
        this.qualifiedName = annotation.getQualifiedName();
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public PsiAnnotation getPsiAnnotation() {
        return psiAnnotation;
    }

    @Override
    public String toString() {
        return "AnnotationEntry{" + "qualifiedName='" + qualifiedName + '\'' + ", psiAnnotation=" + psiAnnotation + '}';
    }
}
