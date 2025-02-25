package me.lotabout.codegenerator.util;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.generate.element.ElementFactory;
import org.jetbrains.java.generate.element.FieldElement;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiTypeElement;

/**
 * Wrapper around FieldElement that provides caching and utility methods for field information.
 * This class is immutable and thread-safe.
 */
public class FieldEntry implements MemberEntry<PsiField> {
    // Use ConcurrentHashMap as cache to ensure thread safety
    private static final ConcurrentHashMap<PsiField, WeakReference<FieldEntry>> CACHE = new ConcurrentHashMap<>();

    /**
     * Factory method to create or retrieve a FieldEntry instance.
     * Uses caching to avoid creating duplicate instances for the same PsiField.
     *
     * @param field The PsiField to create a FieldEntry for
     * @param useAccessor Whether to use accessor methods
     * @return A new or cached FieldEntry instance
     */
    public static FieldEntry of(final PsiField field, final boolean useAccessor) {
        if (field == null) {
            return null;
        }
        // Try to get from cache first
        final WeakReference<FieldEntry> ref = CACHE.get(field);
        FieldEntry entry = ref != null ? ref.get() : null;
        if (entry != null) {
            return entry;
        }
        // Create new instance if not in cache
        entry = new FieldEntry(field, ElementFactory.newFieldElement(field, useAccessor));
        // Use putIfAbsent to ensure thread safety
        final WeakReference<FieldEntry> existing = CACHE.putIfAbsent(field, new WeakReference<>(entry));
        return existing != null && existing.get() != null ? existing.get() : entry;
    }

    private final PsiField raw;
    private final FieldElement element;
    private volatile TypeEntry type;  // Cache for field type

    /**
     * Private constructor to enforce instance creation through factory method.
     * Initializes all fields and caches type information.
     *
     * @param field The PsiField to create a FieldEntry for
     * @param element The FieldElement wrapper
     */
    private FieldEntry(final PsiField field, final FieldElement element) {
        this.raw = field;
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
        final PsiTypeElement psiTypeElement = raw.getTypeElement();
        return TypeEntry.of(psiTypeElement);
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

    public boolean isModifierTransient() {
        return element.isModifierTransient();
    }

    public boolean isModifierVolatile() {
        return element.isModifierVolatile();
    }

    @Nullable
    public TypeEntry getElementType() {
        return type != null ? type.getElementType() : null;
    }

    @Nullable
    public TypeEntry getKeyType() {
        return type != null ? type.getKeyType() : null;
    }

    @Nullable
    public TypeEntry getValueType() {
        return type != null ? type.getValueType() : null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldEntry)) return false;
        final FieldEntry that = (FieldEntry) o;
        return raw.equals(that.raw) &&
                element.equals(that.element) &&
                (type == null ? that.type == null : type.equals(that.type));
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
        return "FieldEntry{" +
                "raw=" + raw +
                ", element=" + element +
                ", type=" + type +
                ", name='" + getName() + '\'' +
                ", fieldType='" + getTypeName() + '\'' +
                ", isConstant=" + isConstant() +
                ", isEnum=" + isEnum() +
                '}';
    }
}
