package me.lotabout.codegenerator.util;

import com.intellij.psi.PsiJvmModifiersOwner;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;

public class AnnotationUtil {
    private AnnotationUtil() {
    }

    public static boolean isAnnotatedWith(@Nonnull PsiJvmModifiersOwner psiJvmModifiersOwner, @Nonnull String qualifiedName) {
        return Arrays.stream(psiJvmModifiersOwner.getAnnotations())
                .filter(annotation -> Objects.nonNull(annotation.getQualifiedName()))
                .anyMatch(annotation -> annotation.getQualifiedName().equals(qualifiedName));
    }
}
