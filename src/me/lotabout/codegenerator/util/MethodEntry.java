package me.lotabout.codegenerator.util;

import com.intellij.psi.PsiMethod;
import org.jetbrains.java.generate.element.MethodElement;

/**
 * Wrapper around MethodElement
 */
public class MethodEntry implements MemberEntry<PsiMethod> {
    PsiMethod raw;
    MethodElement element;

    public MethodEntry(PsiMethod field, MethodElement element) {
        this.raw = field;
        this.element = element;
    }

    public MethodElement getElement() {
        return element;
    }

    public PsiMethod getRaw() {
        return raw;
    }

    public String getMethodName() {
        return element.getMethodName();
    }

    public void setMethodName(String s) {
        element.setMethodName(s);
    }

    public String getFieldName() {
        return element.getFieldName();
    }

    public void setFieldName(String s) {
        element.setFieldName(s);
    }

    public String getAccessor() {
        return element.getAccessor();
    }

    public boolean isModifierAbstract() {
        return element.isModifierAbstract();
    }

    public void setModifierAbstract(boolean b) {
        element.setModifierAbstract(b);
    }

    public boolean isModifierSynchronzied() {
        return element.isModifierSynchronzied();
    }

    public boolean isModifierSynchronized() {
        return element.isModifierSynchronized();
    }

    public void setModifierSynchronized(boolean b) {
        element.setModifierSynchronized(b);
    }

    public boolean isReturnTypeVoid() {
        return element.isReturnTypeVoid();
    }

    public void setReturnTypeVoid(boolean b) {
        element.setReturnTypeVoid(b);
    }

    public boolean isGetter() {
        return element.isGetter();
    }

    public void setGetter(boolean b) {
        element.setGetter(b);
    }

    public boolean isDeprecated() {
        return element.isDeprecated();
    }

    public void setDeprecated(boolean b) {
        element.setDeprecated(b);
    }

    public boolean matchName(String s) throws IllegalArgumentException {
        return element.matchName(s);
    }

    public String getName() {
        return element.getName();
    }

    public boolean isArray() {
        return element.isArray();
    }

    public boolean isNestedArray() {
        return element.isNestedArray();
    }

    public void setNestedArray(boolean b) {
        element.setNestedArray(b);
    }

    public boolean isCollection() {
        return element.isCollection();
    }

    public boolean isMap() {
        return element.isMap();
    }

    public boolean isPrimitive() {
        return element.isPrimitive();
    }

    public boolean isString() {
        return element.isString();
    }

    public boolean isPrimitiveArray() {
        return element.isPrimitiveArray();
    }

    public boolean isObjectArray() {
        return element.isObjectArray();
    }

    public boolean isNumeric() {
        return element.isNumeric();
    }

    public boolean isObject() {
        return element.isObject();
    }

    public boolean isDate() {
        return element.isDate();
    }

    public boolean isSet() {
        return element.isSet();
    }

    public boolean isList() {
        return element.isList();
    }

    public boolean isStringArray() {
        return element.isStringArray();
    }

    public boolean isCalendar() {
        return element.isCalendar();
    }

    public String getTypeName() {
        return element.getTypeName();
    }

    public String getTypeQualifiedName() {
        return element.getTypeQualifiedName();
    }

    public String getType() {
        return element.getType();
    }

    public void setType(String s) {
        element.setType(s);
    }

    public boolean isBoolean() {
        return element.isBoolean();
    }

    public boolean isLong() {
        return element.isLong();
    }

    public void setLong(boolean b) {
        element.setLong(b);
    }

    public boolean isFloat() {
        return element.isFloat();
    }

    public void setFloat(boolean b) {
        element.setFloat(b);
    }

    public boolean isDouble() {
        return element.isDouble();
    }

    public void setDouble(boolean b) {
        element.setDouble(b);
    }

    public boolean isVoid() {
        return element.isVoid();
    }

    public boolean isNotNull() {
        return element.isNotNull();
    }

    public void setNotNull(boolean b) {
        element.setNotNull(b);
    }

    public void setVoid(boolean b) {
        element.setVoid(b);
    }

    public boolean isChar() {
        return element.isChar();
    }

    public void setChar(boolean b) {
        element.setChar(b);
    }

    public boolean isByte() {
        return element.isByte();
    }

    public void setByte(boolean b) {
        element.setByte(b);
    }

    public boolean isShort() {
        return element.isShort();
    }

    public void setShort(boolean b) {
        element.setShort(b);
    }

    public void setBoolean(boolean b) {
        element.setBoolean(b);
    }

    public void setName(String s) {
        element.setName(s);
    }

    public boolean isModifierStatic() {
        return element.isModifierStatic();
    }

    public boolean isModifierPublic() {
        return element.isModifierPublic();
    }

    public boolean isModifierProtected() {
        return element.isModifierProtected();
    }

    public boolean isModifierPackageLocal() {
        return element.isModifierPackageLocal();
    }

    public boolean isModifierPrivate() {
        return element.isModifierPrivate();
    }

    public boolean isModifierFinal() {
        return element.isModifierFinal();
    }

    @Override
    public String toString() {
        return "MethodEntry{" +
                "raw=" + raw +
                ", element=" + element +
                '}';
    }
}
