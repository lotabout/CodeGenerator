package me.lotabout.codegenerator.util;

import com.intellij.psi.PsiField;
import org.jetbrains.java.generate.element.FieldElement;

/**
 * Wrapper around FieldElement
 */
public class FieldEntry implements MemberEntry<PsiField> {
    private PsiField raw;
    private FieldElement element;

    public FieldEntry(PsiField field, FieldElement element) {
        this.raw = field;
        this.element = element;
    }

    public PsiField getRaw() {
        return raw;
    }

    public FieldElement getElement() {
        return element;
    }

    public String getAccessor() {
        return element.getAccessor();
    }

    public boolean isConstant() {
        return element.isConstant();
    }

    public boolean isModifierTransient() {
        return element.isModifierTransient();
    }

    public boolean isModifierVolatile() {
        return element.isModifierVolatile();
    }

    public boolean isEnum() {
        return element.isEnum();
    }

    public void setEnum(boolean b) {
        element.setEnum(b);
    }

    public boolean matchName(String s) throws IllegalArgumentException {
        return element.matchName(s);
    }

    public void setAccessor(String s) {
        element.setAccessor(s);
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
        return "FieldEntry{" +
                "raw=" + raw +
                ", element=" + element +
                '}';
    }
}
