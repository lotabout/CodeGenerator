package me.lotabout.codegenerator.util;

import com.intellij.psi.PsiJvmModifiersOwner;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class AnnotationUtil {
    private AnnotationUtil() {
    }

    public static boolean isAnnotatedWith(@NotNull PsiJvmModifiersOwner psiJvmModifiersOwner, @NotNull String qualifiedName) {
        return Arrays.stream(psiJvmModifiersOwner.getAnnotations())
                .filter(annotation -> Objects.nonNull(annotation.getQualifiedName()))
                .anyMatch(annotation -> annotation.getQualifiedName().equals(qualifiedName));
    }
}
