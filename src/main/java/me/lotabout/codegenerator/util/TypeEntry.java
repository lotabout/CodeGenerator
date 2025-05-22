package me.lotabout.codegenerator.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.generate.psi.PsiAdapter;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.PsiTypeParameter;

/**
 * A wrapper for PsiType that provides caching and utility methods for type information.
 * This class is immutable and thread-safe.
 * <p>
 * This class can represents Class, primitive types, and array types.
 *
 * @author lotabout, Haixing Hu
 */
public class TypeEntry {

    // Use ConcurrentHashMap as cache to ensure thread safety
    // private static final Map<PsiType, TypeEntry> CACHE = new ConcurrentHashMap<>();

    /**
     * Factory method to create or retrieve a {@link TypeEntry} instance.
     *
     * @param type The PsiType to create a {@link TypeEntry} for
     * @return A new or cached {@link TypeEntry} instance
     */
    public static TypeEntry of(final PsiType type, final JavaPsiFacade facade) {
        if (type == null) {
            return null;
        }
        // return CACHE.computeIfAbsent(type, (t) -> new TypeEntry(t, facade));
        return new TypeEntry(type, facade);
    }

    /**
     * Factory method to create or retrieve a {@link TypeEntry} instance.
     *
     * @param psiClass
     *      The PsiClass to create a {@link TypeEntry} for
     * @return
     *      A new or cached {@link TypeEntry} instance
     */
    public static TypeEntry of(final PsiClass psiClass) {
        if (psiClass == null) {
            return null;
        }
        final JavaPsiFacade facade = JavaPsiFacade.getInstance(psiClass.getProject());
        final PsiElementFactory factory = facade.getElementFactory();
        final PsiType type = factory.createType(psiClass);
        // return CACHE.computeIfAbsent(type, (t) -> new TypeEntry(t, facade));
        return new TypeEntry(type, facade);
    }

    /**
     * Factory method to create or retrieve a {@link TypeEntry} instance.
     *
     * @param psiTypeElement
     *      The PsiTypeElement to create a {@link TypeEntry} for
     * @return
     *      A new or cached {@link TypeEntry} instance
     */
    public static TypeEntry of(final PsiTypeElement psiTypeElement) {
        if (psiTypeElement == null) {
            return null;
        }
        final PsiType type = psiTypeElement.getType();
        final JavaPsiFacade facade = JavaPsiFacade.getInstance(psiTypeElement.getProject());
        // return CACHE.computeIfAbsent(type, (t) -> new TypeEntry(t, facade));
        return new TypeEntry(type, facade);
    }

    private transient final Map<String, Boolean> implementsCache = new ConcurrentHashMap<>();
    private final PsiType type;             // Store the actual type information including generics
    private final JavaPsiFacade facade;
    @Nullable
    private final PsiClass psiClass;        // Store the resolved PsiClass for this type, which may be null
    private final boolean primitiveArray;
    private final boolean objectArray;
    private final boolean stringArray;
    private final boolean array;
    private final boolean collection;
    private final boolean set;
    private final boolean map;
    private final boolean primitive;
    private final boolean voidType;
    private final boolean enumType;
    private final boolean exceptionType;
    private final boolean abstractType;
    private final boolean deprecated;
    private final boolean interfaceType;
    private final boolean recordType;
    private final String simpleName;
    private final String qualifiedName;
    private final String packageName;

    private volatile TypeEntry superClass;
    private volatile TypeEntry elementType;
    private volatile TypeEntry keyType;
    private volatile TypeEntry valueType;
    private volatile List<FieldEntry> fields;
    private volatile List<FieldEntry> allFields;
    private volatile List<MethodEntry> methods;
    private volatile List<MethodEntry> allMethods;
    private volatile List<MemberEntry<?>> members;
    private volatile List<MemberEntry<?>> allMembers;
    private volatile Set<TypeEntry> interfaces;
    private volatile List<TypeEntry> superClasses;
    private volatile List<TypeEntry> typeParameters;

    /**
     * Private constructor to enforce instance creation through factory method.
     * Initializes all fields and caches type information.
     *
     * @param type
     *      The PsiType to create a {@link TypeEntry} for
     */
    private TypeEntry(final PsiType type, final JavaPsiFacade facade) {
        this.type = type;
        this.facade = facade;
        final PsiElementFactory factory = facade.getElementFactory();
        this.psiClass = ((type instanceof PsiClassType) ? ((PsiClassType) type).resolve() : null);
        // this.raw = element;
        // this.element = (raw == null ? null : ElementFactory.newClassElement(raw));
        this.primitiveArray = PsiAdapter.isPrimitiveArrayType(type);
        this.objectArray = PsiAdapter.isObjectArrayType(type);
        this.stringArray = PsiAdapter.isStringArrayType(type);
        this.array = (type instanceof PsiArrayType);
        this.collection = PsiAdapter.isCollectionType(factory, type);
        this.set = PsiAdapter.isSetType(factory, type);
        this.map = PsiAdapter.isMapType(factory, type);
        this.primitive = PsiAdapter.isPrimitiveType(type);
        this.voidType = PsiAdapter.isTypeOfVoid(type);
        this.enumType = (psiClass != null && psiClass.isEnum());
        this.exceptionType = PsiAdapter.isExceptionClass(psiClass);
        this.abstractType = (psiClass != null && psiClass.hasModifierProperty(PsiModifier.ABSTRACT));
        this.deprecated = (psiClass != null && psiClass.isDeprecated());
        this.interfaceType = (psiClass != null && psiClass.isInterface());
        this.recordType = (psiClass != null && psiClass.isRecord());
        this.simpleName = type.getPresentableText();
        this.qualifiedName = type.getCanonicalText();
        this.packageName = GenerationUtil.getPackageName(this.qualifiedName);
        // Initialize collections as null for lazy initialization
        this.fields = null;
        this.allFields = null;
        this.methods = null;
        this.allMethods = null;
        this.members = null;
        this.allMembers = null;
        this.superClasses = null;
        this.interfaces = null;
        this.typeParameters = null;
    }

    @Nullable
    private TypeEntry getOrInitValueType() {
        TypeEntry result = valueType;
        if (result == null) {
            synchronized (this) {
                result = valueType;
                if (result == null) {
                    valueType = result = initValueType();
                }
            }
        }
        return result;
    }

    /**
     * Get the element type if this class is a collection or array.
     *
     * @return a {@link TypeEntry} representing the element type if this class is a collection or array,
     *         null otherwise
     */
    @Nullable
    public TypeEntry getElementType() {
        if (array || collection) {
            TypeEntry result = elementType;
            if (result == null) {
                synchronized (this) {
                    result = elementType;
                    if (result == null) {
                        elementType = result = initElementType();
                    }
                }
            }
            return result;
        }
        return null;
    }

    @Nullable
    private TypeEntry initElementType() {
        if (array) {
            return initArrayElementType();
        }
        if (collection) {
            return initCollectionElementType();
        }
        return null;
    }

    @Nullable
    private TypeEntry initArrayElementType() {
        final String componentName = type.getCanonicalText();
        final String baseType = componentName.substring(0, componentName.length() - 2);
        final PsiClass componentClass = facade.findClass(baseType, psiClass.getResolveScope());
        return componentClass != null ? of(componentClass) : null;
    }

    @Nullable
    private TypeEntry initCollectionElementType() {
        if ((type instanceof PsiClassType) && (psiClass != null)) {
            final PsiClassType classType = (PsiClassType) type;
            final PsiType[] parameters = classType.getParameters();
            if (parameters.length > 0) {
                return of(parameters[0], facade);
            }
        }
        return null;
    }

    /**
     * Get the key type if this class is a map.
     *
     * @return a {@link TypeEntry} representing the key type if this class is a map,
     *         null otherwise
     */
    @Nullable
    public TypeEntry getKeyType() {
        if (map && (psiClass != null)) {
            TypeEntry result = keyType;
            if (result == null) {
                synchronized (this) {
                    result = keyType;
                    if (result == null) {
                        keyType = result = initKeyType();
                    }
                }
            }
            return result;
        }
        return null;
    }

    @Nullable
    private TypeEntry initKeyType() {
        final PsiClassType classType = (PsiClassType) type;
        final PsiType[] parameters = classType.getParameters();
        if (parameters.length > 0) {
            return of(parameters[0], facade);
        }
        return null;
    }

    /**
     * Get the value type if this class is a map.
     *
     * @return a {@link TypeEntry} representing the value type if this class is a map,
     *         null otherwise
     */
    @Nullable
    public TypeEntry getValueType() {
        if (map && (psiClass != null)) {
            TypeEntry result = valueType;
            if (result == null) {
                synchronized (this) {
                    result = valueType;
                    if (result == null) {
                        valueType = result = initValueType();
                    }
                }
            }
            return result;
        }
        return null;
    }

    @Nullable
    private TypeEntry initValueType() {
        final PsiClassType classType = (PsiClassType) type;
        final PsiType[] parameters = classType.getParameters();
        if (parameters.length > 1) {
            return of(parameters[1], facade);
        }
        return null;
    }

    /**
     * Get the list of fields declared in this class.
     *
     * @return List of FieldEntry objects
     */
    public List<FieldEntry> getFields() {
        List<FieldEntry> result = fields;
        if (result == null) {
            synchronized (this) {
                result = fields;
                if (result == null) {
                    fields = result = initFields();
                }
            }
        }
        return result;
    }

    private List<FieldEntry> initFields() {
        if (psiClass != null) {
            return GenerationUtil.getFields(psiClass);
        }
        return Collections.emptyList();
    }

    /**
     * Get all fields including inherited ones.
     *
     * @return List of all FieldEntry objects
     */
    public List<FieldEntry> getAllFields() {
        List<FieldEntry> result = allFields;
        if (result == null) {
            synchronized (this) {
                result = allFields;
                if (result == null) {
                    allFields = result = initAllFields();
                }
            }
        }
        return result;
    }

    private List<FieldEntry> initAllFields() {
        if (psiClass != null) {
            return GenerationUtil.getAllFields(psiClass);
        }
        return Collections.emptyList();
    }

    /**
     * Get methods declared in this class.
     *
     * @return List of MethodEntry objects
     */
    public List<MethodEntry> getMethods() {
        List<MethodEntry> result = methods;
        if (result == null) {
            synchronized (this) {
                result = methods;
                if (result == null) {
                    methods = result = initMethods();
                }
            }
        }
        return result;
    }

    private List<MethodEntry> initMethods() {
        if (psiClass != null) {
            return GenerationUtil.getMethods(psiClass);
        }
        return Collections.emptyList();
    }

    /**
     * Get all methods including inherited ones.
     *
     * @return List of all MethodEntry objects
     */
    public List<MethodEntry> getAllMethods() {
        List<MethodEntry> result = allMethods;
        if (result == null) {
            synchronized (this) {
                result = allMethods;
                if (result == null) {
                    allMethods = result = initAllMethods();
                }
            }
        }
        return result;
    }

    private List<MethodEntry> initAllMethods() {
        if (psiClass != null) {
            return GenerationUtil.getAllMethods(psiClass);
        }
        return Collections.emptyList();
    }

    /**
     * Get members (fields and methods) declared in this class.
     *
     * @return List of MemberEntry objects
     */
    public List<MemberEntry<?>> getMembers() {
        List<MemberEntry<?>> result = members;
        if (result == null) {
            synchronized (this) {
                result = members;
                if (result == null) {
                    members = result = initMembers();
                }
            }
        }
        return result;
    }

    private List<MemberEntry<?>> initMembers() {
        if (psiClass != null) {
            final List<FieldEntry> fieldMembers = GenerationUtil.getFields(psiClass);
            final List<MethodEntry> methodMembers = GenerationUtil.getMethods(psiClass);
            final List<MemberEntry<?>> result = new ArrayList<>();
            result.addAll(fieldMembers);
            result.addAll(methodMembers);
            return result;
        }
        return Collections.emptyList();
    }

    /**
     * Get all members including inherited ones.
     *
     * @return List of all MemberEntry objects
     */
    public List<MemberEntry<?>> getAllMembers() {
        List<MemberEntry<?>> result = allMembers;
        if (result == null) {
            synchronized (this) {
                result = allMembers;
                if (result == null) {
                    allMembers = result = initAllMembers();
                }
            }
        }
        return result;
    }


    private List<MemberEntry<?>> initAllMembers() {
        if (psiClass != null) {
            final List<FieldEntry> fieldMembers = GenerationUtil.getAllFields(psiClass);
            final List<MethodEntry> methodMembers = GenerationUtil.getAllMethods(psiClass);
            final List<MemberEntry<?>> result = new ArrayList<>();
            result.addAll(fieldMembers);
            result.addAll(methodMembers);
            return result;
        }
        return Collections.emptyList();
    }

    /**
     * Get the simple name of this type.
     *
     * @return The simple type name
     */
    public String getName() {
        return simpleName;
    }

    /**
     * Get the qualified name of this type.
     *
     * @return The qualified type name
     */
    public String getQualifiedName() {
        return qualifiedName;
    }

    /**
     * Get the package name of this class.
     *
     * @return The package name as a string
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Get inner classes declared in this class.
     *
     * @return
     *      List of {@link TypeEntry} objects for inner classes
     */
    public List<TypeEntry> getInnerClasses() {
        if (psiClass != null) {
            return GenerationUtil.getInnerClasses(psiClass);
        }
        return Collections.emptyList();
    }

    /**
     * Get all inner classes including inherited ones.
     *
     * @return
     *      List of all {@link TypeEntry} objects for inner classes
     */
    public List<TypeEntry> getAllInnerClasses() {
        if (psiClass != null) {
            return GenerationUtil.getAllInnerClasses(psiClass);
        }
        return Collections.emptyList();
    }

    /**
     * Get the superclass of this class.
     *
     * @return
     *      {@link TypeEntry} for the superclass, or null if none exists
     */
    @Nullable
    public TypeEntry getSuperClass() {
        if (psiClass == null) {
            return null;
        }
        if (psiClass.getSuperClass() == null) {
            return null;
        }
        TypeEntry result = superClass;
        if (result == null) {
            synchronized (this) {
                result = superClass;
                if (result == null) {
                    superClass = result = of(psiClass.getSuperClass());
                }
            }
        }
        return result;
    }

    /**
     * Check if this class has a superclass.
     *
     * @return true if has superclass, false otherwise
     */
    public boolean hasSuper() {
        return getSuperClass() != null;
    }

    /**
     * Get the simple name of the superclass.
     *
     * @return
     *      The superclass simple name
     */
    @Nullable
    public String getSuperName() {
        final TypeEntry theSuperClass = getSuperClass();
        return (theSuperClass == null ? null : theSuperClass.getName());
    }

    /**
     * Get the qualified name of the superclass.
     *
     * @return The superclass qualified name
     */
    @Nullable
    public String getSuperQualifiedName() {
        final TypeEntry theSuperClass = getSuperClass();
        return (theSuperClass == null ? null : theSuperClass.getQualifiedName());
    }

    /**
     * Check if this class implements the specified interface.
     * <p>
     * This function checks direct implementations, interface hierarchy, and
     * superclass implementations.
     *
     * @param interfaceName
     *      The interface name to check, it must be fully qualified.
     * @return
     *      true if the class implements the interface, false otherwise
     */
    public boolean isImplements(final String interfaceName) {
        return implementsCache.computeIfAbsent(interfaceName, this::checkImplements);
    }

    private boolean checkImplements(final String interfaceName) {
        if (psiClass != null) {
            final Set<TypeEntry> allInterfaces = getInterfaces();
            for (final TypeEntry iface : allInterfaces) {
                if (interfaceName.equals(iface.getQualifiedName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the set of interfaces implemented by this class.
     *
     * @return
     *     the set of interfaces implemented by this class, or an empty set if
     *     this type is not a class type.
     */
    public Set<TypeEntry> getInterfaces() {
        if (psiClass == null) {
            return Collections.emptySet();
        }
        Set<TypeEntry> result = interfaces;
        if (result == null) {
            synchronized (this) {
                result = interfaces;
                if (result == null) {
                    interfaces = result = initInterfaces();
                }
            }
        }
        return result;
    }

    private Set<TypeEntry> initInterfaces() {
        assert psiClass != null;
        final Set<PsiClass> allInterfaces = GenerationUtil.getAllInterfaces(psiClass);
        final Set<TypeEntry> result = new HashSet<>();
        for (final PsiClass iface : allInterfaces) {
            result.add(of(iface));
        }
        return result;
    }

    /**
     * Get the names of interfaces implemented by this class.
     *
     * @return Array of interface names
     */
    public String[] getInterfaceNames() {
        final Set<TypeEntry> allInterfaces = getInterfaces();
        final String[] result = new String[allInterfaces.size()];
        int i = 0;
        for (final TypeEntry iface : allInterfaces) {
            result[i++] = iface.getQualifiedName();
        }
        return result;
    }

    /**
     * Check if this class extends any of the given classnames.
     *
     * @param classNames
     *      list of class names seperated by comma.
     * @return
     *      true if this class extends one of the given classnames.
     */
    public boolean isExtends(final String classNames) {
        final String superName = getSuperName();
        final String[] superNames = classNames.split(",");
        for (final String name : superNames) {
            if (name.equals(superName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if this class extends any of the given classnames.
     *
     * @param classNames
     *      list of class names.
     * @return
     *      true if this class extends one of the given classnames.
     */
    public boolean isExtends(final String... classNames) {
        final String superName = getSuperName();
        for (final String name : classNames) {
            if (name.equals(superName)) {
                return true;
            }
        }
        return false;
    }

    public List<TypeEntry> getSuperClasses() {
        List<TypeEntry> result = superClasses;
        if (result == null) {
            synchronized (this) {
                result = superClasses;
                if (result == null) {
                    superClasses = result = initSuperClasses();
                }
            }
        }
        return result;
    }

    private List<TypeEntry> initSuperClasses() {
        if (psiClass != null) {
            final PsiClass[] supers = psiClass.getSupers();
            final List<TypeEntry> result = new ArrayList<>();
            for (final PsiClass s : supers) {
                result.add(of(s));
            }
            return result;
        }
        return Collections.emptyList();
    }

    /**
     * Get the simple names of super classes of this class.
     *
     * @return
     *     Array of super class simple names of this class, or an empty array
     *     if this type is not a class or it has no super classes.
     */
    public String[] getSuperClassNames() {
        final List<TypeEntry> supers = getSuperClasses();
        final String[] result = new String[supers.size()];
        int i = 0;
        for (final TypeEntry s : supers) {
            result[i++] = s.getName();
        }
        return result;
    }

    /**
     * Get the qualified names of super classes of this class.
     *
     * @return
     *     Array of super class qualified names of this class, or an empty array
     *     if this type is not a class or it has no super classes.
     */
    public String[] getSuperClassQualifiedNames() {
        final List<TypeEntry> supers = getSuperClasses();
        final String[] result = new String[supers.size()];
        int i = 0;
        for (final TypeEntry s : supers) {
            result[i++] = s.getQualifiedName();
        }
        return result;
    }

    /**
     * Get the type parameters of this class.
     *
     * @return
     *     List of type parameters of this class, or an empty list if this
     *     type is not a class or it has no type parameters.
     */
    public List<TypeEntry> getTypeParameters() {
        if (psiClass == null) {
            return Collections.emptyList();
        }
        List<TypeEntry> result = typeParameters;
        if (result == null) {
            synchronized (this) {
                result = typeParameters;
                if (result == null) {
                    typeParameters = result = initTypeParameters();
                }
            }
        }
        return result;
    }

    private List<TypeEntry> initTypeParameters() {
        assert psiClass != null;
        final PsiTypeParameter[] parameters = psiClass.getTypeParameters();
        final List<TypeEntry> result = new ArrayList<>();
        for (final PsiTypeParameter param : parameters) {
            result.add(of(param));
        }
        return result;
    }

    /**
     * Get the names of type parameters of this class.
     *
     * @return
     *     Array of type parameter names of this class, or an empty array if
     *     this type is not a class or it has no type parameters.
     */
    public String[] getTypeParameterNames() {
        final List<TypeEntry> params = getTypeParameters();
        final String[] result = new String[params.size()];
        int i = 0;
        for (final TypeEntry param : params) {
            result[i++] = param.getName();
        }
        return result;
    }

    /**
     * Check if this class is a primitive type.
     *
     * @return true if primitive, false otherwise
     */
    public boolean isPrimitive() {
        return primitive;
    }

    /**
     * Check if this class is a void type.
     *
     * @return true if void, false otherwise
     */
    public boolean isVoid() {
        return voidType;
    }

    /**
     * Check if this class is an array type.
     *
     * @return true if array, false otherwise
     */
    public boolean isArray() {
        return array;
    }

    /**
     * Check if this class is a primitive array type.
     *
     * @return true if primitive array, false otherwise
     */
    public boolean isPrimitiveArray() {
        return primitiveArray;
    }

    /**
     * Check if this class is an object array type.
     *
     * @return true if object array, false otherwise
     */
    public boolean isObjectArray() {
        return objectArray;
    }

    /**
     * Check if this class is an string array type.
     *
     * @return true if string array, false otherwise
     */
    public boolean isStringArray() {
        return stringArray;
    }

    /**
     * Check if this class is a collection type.
     *
     * @return true if collection, false otherwise
     */
    public boolean isCollection() {
        return collection;
    }

    /**
     * Check if this class is a set type.
     *
     * @return true if set, false otherwise
     */
    public boolean isSet() {
        return set;
    }

    /**
     * Check if this class is a map type.
     *
     * @return true if map, false otherwise
     */
    public boolean isMap() {
        return map;
    }

    /**
     * Check if this class is deprecated.
     *
     * @return true if deprecated, false otherwise
     */
    public boolean isDeprecated() {
        return deprecated;
    }

    /**
     * Check if this class is an interface.
     *
     * @return true if it is an interface, false otherwise
     */
    public boolean isInterface() {
        return interfaceType;
    }

    /**
     * Check if this class is a record.
     *
     * @return true if it is a record, false otherwise
     */
    public boolean isRecord() {
        return recordType;
    }

    /**
     * Check if this class is an enum.
     *
     * @return true if enum, false otherwise
     */
    public boolean isEnum() {
        return enumType;
    }

    /**
     * Check if this class is an exception.
     *
     * @return true if exception, false otherwise
     */
    public boolean isException() {
        return exceptionType;
    }

    /**
     * Check if this class is abstract.
     *
     * @return true if abstract, false otherwise
     */
    public boolean isAbstract() {
        return abstractType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TypeEntry typeEntry = (TypeEntry) o;
        return new EqualsBuilder().append(type, typeEntry.type).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(type).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("type", type)
            .append("facade", facade)
            .append("psiClass", psiClass)
            .append("primitiveArray", primitiveArray)
            .append("objectArray", objectArray)
            .append("stringArray", stringArray)
            .append("array", array)
            .append("collection", collection)
            .append("set", set)
            .append("map", map)
            .append("primitive", primitive)
            .append("voidType", voidType)
            .append("enumType", enumType)
            .append("exceptionType", exceptionType)
            .append("abstractType", abstractType)
            .append("deprecated", deprecated)
            .append("interfaceType", interfaceType)
            .append("recordType", recordType)
            .append("simpleName", simpleName)
            .append("qualifiedName", qualifiedName)
            .append("packageName", packageName)
            .append("superClass", superClass)
            .append("elementType", elementType)
            .append("keyType", keyType)
            .append("valueType", valueType)
            .append("fields", fields)
            .append("allFields", allFields)
            .append("methods", methods)
            .append("allMethods", allMethods)
            .append("members", members)
            .append("allMembers", allMembers)
            .append("interfaces", interfaces)
            .append("superClasses", superClasses)
            .append("typeParameters", typeParameters)
            .toString();
    }
}
