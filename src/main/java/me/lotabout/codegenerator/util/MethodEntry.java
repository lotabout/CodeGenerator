package me.lotabout.codegenerator.util;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.generate.element.ElementFactory;
import org.jetbrains.java.generate.element.MethodElement;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiTypeElement;

/**
 * Wrapper around MethodElement that provides caching and utility methods for method information.
 * This class is immutable and thread-safe.
 */
public class MethodEntry implements MemberEntry<PsiMethod> {
    // Use ConcurrentHashMap as cache to ensure thread safety
    private static final ConcurrentHashMap<PsiMethod, WeakReference<MethodEntry>> CACHE = new ConcurrentHashMap<>();

    /**
     * Factory method to create or retrieve a MethodEntry instance.
     * Uses caching to avoid creating duplicate instances for the same PsiMethod.
     *
     * @param method The PsiMethod to create a MethodEntry for
     * @return A new or cached MethodEntry instance
     */
    public static MethodEntry of(final PsiMethod method) {
        if (method == null) {
            return null;
        }
        // Try to get from cache first
        final WeakReference<MethodEntry> ref = CACHE.get(method);
        MethodEntry entry = ref != null ? ref.get() : null;
        if (entry != null) {
            return entry;
        }
        // Create new instance if not in cache
        entry = new MethodEntry(method, ElementFactory.newMethodElement(method));
        // Use putIfAbsent to ensure thread safety
        final WeakReference<MethodEntry> existing = CACHE.putIfAbsent(method, new WeakReference<>(entry));
        return existing != null && existing.get() != null ? existing.get() : entry;
    }

    private final PsiMethod raw;
    private final MethodElement element;
    private volatile TypeEntry type;

    /**
     * Private constructor to enforce instance creation through factory method.
     * Initializes all fields and caches type information.
     *
     * @param method The PsiMethod to create a MethodEntry for
     * @param element The MethodElement wrapper
     */
    private MethodEntry(final PsiMethod method, final MethodElement element) {
        this.raw = method;
        this.element = element;
    }

    @Override
    public TypeEntry getType() {
        TypeEntry result = type;
        if (result == null) {
            synchronized (this) {
                result = type;
                if (result == null) {
                    type = result = initType();
                }
            }
        }
        return result;
    }

    private TypeEntry initType() {
        if (raw == null) {
            return null;
        }
        final PsiTypeElement psiTypeElement = raw.getReturnTypeElement();
        return TypeEntry.of(psiTypeElement);
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

    public String getFieldName() {
        return element.getFieldName();
    }

    public boolean isModifierAbstract() {
        return element.isModifierAbstract();
    }

    public boolean isModifierSynchronzied() {
        return element.isModifierSynchronzied();
    }

    public boolean isModifierSynchronized() {
        return element.isModifierSynchronized();
    }

    public boolean isReturnTypeVoid() {
        return element.isReturnTypeVoid();
    }

    public boolean isGetter() {
        return element.isGetter();
    }

    public boolean isDeprecated() {
        return element.isDeprecated();
    }

    @Override
    public boolean isAnnotatedWith(@NotNull final String qualifiedName) {
        return AnnotationUtil.isAnnotatedWith(raw, qualifiedName);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodEntry)) return false;
        final MethodEntry that = (MethodEntry) o;
        return raw.equals(that.raw)
            && element.equals(that.element)
            && (type == null ? that.type == null : type.equals(that.type));
    }

    @Override
    public int hashCode() {
        int result = raw.hashCode();
        result = 31 * result + element.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MethodEntry{" +
                "raw=" + raw +
                ", element=" + element +
                ", type=" + type +
                ", name='" + getMethodName() + '\'' +
                ", returnType='" + (type != null ? type.getName() : "void") + '\'' +
                '}';
    }
}
