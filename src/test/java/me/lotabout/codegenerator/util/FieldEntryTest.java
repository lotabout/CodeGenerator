package me.lotabout.codegenerator.util;

import org.jetbrains.java.generate.element.FieldElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.intellij.psi.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FieldEntryTest {

    @Mock
    private PsiField mockField;
    @Mock
    private FieldElement mockElement;
    @Mock
    private PsiArrayType mockArrayType;
    @Mock
    private PsiClassType mockClassType;
    @Mock
    private PsiClass mockClass;
    @Mock
    private PsiClassType mockComponentType;
    @Mock
    private PsiClass mockComponentClass;

    private FieldEntry fieldEntry;

    @BeforeEach
    void setUp() {
        fieldEntry = FieldEntry.of(mockField, false);
    }

    @Test
    void getElementType_NonArrayNonCollection_ReturnsNull() {
        // Given
        when(mockElement.isArray()).thenReturn(false);
        when(mockElement.isCollection()).thenReturn(false);

        // When
        final TypeEntry result = fieldEntry.getElementType();

        // Then
        assertNull(result);
    }

    @Test
    void getElementType_Array_ReturnsComponentType() {
        // Given
        when(mockElement.isArray()).thenReturn(true);
        when(mockElement.isCollection()).thenReturn(false);
        when(mockField.getType()).thenReturn(mockArrayType);
        when(mockArrayType.getComponentType()).thenReturn(mockComponentType);
        when(mockComponentType.resolve()).thenReturn(mockComponentClass);

        // When
        final TypeEntry result = fieldEntry.getElementType();

        // Then
        assertNotNull(result);
        verify(mockField).getType();
        verify(mockArrayType).getComponentType();
        verify(mockComponentType).resolve();
    }

    @Test
    void getElementType_Collection_ReturnsGenericType() {
        // Given
        when(mockElement.isArray()).thenReturn(false);
        when(mockElement.isCollection()).thenReturn(true);
        when(mockField.getType()).thenReturn(mockClassType);
        when(mockClassType.resolveGenerics()).thenReturn(mock(PsiClassType.ClassResolveResult.class));
        when(mockClassType.getParameters()).thenReturn(new PsiType[]{mockComponentType});
        when(mockComponentType.resolve()).thenReturn(mockComponentClass);

        // When
        final TypeEntry result = fieldEntry.getElementType();

        // Then
        assertNotNull(result);
        verify(mockField).getType();
        verify(mockClassType).resolveGenerics();
        verify(mockClassType).getParameters();
        verify(mockComponentType).resolve();
    }

    @Test
    void getElementType_CollectionWithoutGenericType_ReturnsNull() {
        // Given
        when(mockElement.isArray()).thenReturn(false);
        when(mockElement.isCollection()).thenReturn(true);
        when(mockField.getType()).thenReturn(mockClassType);
        when(mockClassType.resolveGenerics()).thenReturn(mock(PsiClassType.ClassResolveResult.class));
        when(mockClassType.getParameters()).thenReturn(new PsiType[0]);

        // When
        final TypeEntry result = fieldEntry.getElementType();

        // Then
        assertNull(result);
        verify(mockField).getType();
        verify(mockClassType).resolveGenerics();
        verify(mockClassType).getParameters();
    }

    @Test
    void getKeyType_NonMap_ReturnsNull() {
        // Given
        when(mockElement.isMap()).thenReturn(false);

        // When
        final TypeEntry result = fieldEntry.getKeyType();

        // Then
        assertNull(result);
    }

    @Test
    void getKeyType_Map_ReturnsKeyType() {
        // Given
        when(mockElement.isMap()).thenReturn(true);
        when(mockField.getType()).thenReturn(mockClassType);
        when(mockClassType.resolveGenerics()).thenReturn(mock(PsiClassType.ClassResolveResult.class));
        when(mockClassType.getParameters()).thenReturn(new PsiType[]{mockComponentType});
        when(mockComponentType.resolve()).thenReturn(mockComponentClass);

        // When
        final TypeEntry result = fieldEntry.getKeyType();

        // Then
        assertNotNull(result);
        verify(mockField).getType();
        verify(mockClassType).resolveGenerics();
        verify(mockClassType).getParameters();
        verify(mockComponentType).resolve();
    }

    @Test
    void getKeyType_MapWithoutGenericType_ReturnsNull() {
        // Given
        when(mockElement.isMap()).thenReturn(true);
        when(mockField.getType()).thenReturn(mockClassType);
        when(mockClassType.resolveGenerics()).thenReturn(mock(PsiClassType.ClassResolveResult.class));
        when(mockClassType.getParameters()).thenReturn(new PsiType[0]);

        // When
        final TypeEntry result = fieldEntry.getKeyType();

        // Then
        assertNull(result);
        verify(mockField).getType();
        verify(mockClassType).resolveGenerics();
        verify(mockClassType).getParameters();
    }

    @Test
    void getValueType_NonMap_ReturnsNull() {
        // Given
        when(mockElement.isMap()).thenReturn(false);

        // When
        final TypeEntry result = fieldEntry.getValueType();

        // Then
        assertNull(result);
    }

    @Test
    void getValueType_Map_ReturnsValueType() {
        // Given
        when(mockElement.isMap()).thenReturn(true);
        when(mockField.getType()).thenReturn(mockClassType);
        when(mockClassType.resolveGenerics()).thenReturn(mock(PsiClassType.ClassResolveResult.class));
        when(mockClassType.getParameters()).thenReturn(new PsiType[]{mock(PsiType.class), mockComponentType});
        when(mockComponentType.resolve()).thenReturn(mockComponentClass);

        // When
        final TypeEntry result = fieldEntry.getValueType();

        // Then
        assertNotNull(result);
        verify(mockField).getType();
        verify(mockClassType).resolveGenerics();
        verify(mockClassType).getParameters();
        verify(mockComponentType).resolve();
    }

    @Test
    void getValueType_MapWithoutGenericType_ReturnsNull() {
        // Given
        when(mockElement.isMap()).thenReturn(true);
        when(mockField.getType()).thenReturn(mockClassType);
        when(mockClassType.resolveGenerics()).thenReturn(mock(PsiClassType.ClassResolveResult.class));
        when(mockClassType.getParameters()).thenReturn(new PsiType[]{mock(PsiType.class)});

        // When
        final TypeEntry result = fieldEntry.getValueType();

        // Then
        assertNull(result);
        verify(mockField).getType();
        verify(mockClassType).resolveGenerics();
        verify(mockClassType).getParameters();
    }
}
