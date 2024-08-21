package me.lotabout.codegenerator.util;

import java.util.Arrays;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.intellij.psi.PsiJvmModifiersOwner;

public class AnnotationUtil {
    private AnnotationUtil() {
    }

    public static boolean isAnnotatedWith(@NotNull final PsiJvmModifiersOwner psiJvmModifiersOwner,
            @NotNull final String qualifiedName) {
        return Arrays.stream(psiJvmModifiersOwner.getAnnotations())
                .filter(annotation -> Objects.nonNull(annotation.getQualifiedName()))
                .anyMatch(annotation -> annotation.getQualifiedName().equals(qualifiedName));
    }
}
