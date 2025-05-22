package me.lotabout.codegenerator.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Disabled
public class ClassEntryTest {

    @Mock
    private PsiClass mockClass;
    @Mock
    private PsiClass mockInterface1;
    @Mock
    private PsiClass mockInterface2;
    @Mock
    private PsiClass mockSuperClass;
    @Mock
    private PsiClass mockSuperInterface;

    private TypeEntry classEntry;

    @BeforeEach
    void setUp() {
        when(mockClass.getContainingFile()).thenReturn(mock(PsiJavaFile.class));
        classEntry = TypeEntry.of(mockClass);
    }

    @Test
    void isImplements_DirectInterfaceMatch_ReturnsTrue() {
        // Given
        when(mockClass.getInterfaces()).thenReturn(new PsiClass[]{mockInterface1});
        when(mockInterface1.getQualifiedName()).thenReturn("com.example.TestInterface");
        when(mockInterface1.getName()).thenReturn("TestInterface");

        // When
        final boolean result = classEntry.isImplements("TestInterface");

        // Then
        assertTrue(result);
        verify(mockClass).getInterfaces();
        verify(mockInterface1).getQualifiedName();
        verify(mockInterface1).getName();
    }

    @Test
    void isImplements_IndirectInterfaceImplementation_ReturnsTrue() {
        // Given
        when(mockClass.getInterfaces()).thenReturn(new PsiClass[]{mockInterface1});
        when(mockInterface1.getQualifiedName()).thenReturn("com.example.Interface1");
        when(mockInterface1.getName()).thenReturn("Interface1");
        when(mockInterface1.getInterfaces()).thenReturn(new PsiClass[]{mockInterface2});
        when(mockInterface2.getQualifiedName()).thenReturn("com.example.TestInterface");
        when(mockInterface2.getName()).thenReturn("TestInterface");

        // When
        final boolean result = classEntry.isImplements("TestInterface");

        // Then
        assertTrue(result);
        verify(mockClass).getInterfaces();
        verify(mockInterface1).getInterfaces();
        verify(mockInterface2).getQualifiedName();
        verify(mockInterface2).getName();
    }

    @Test
    void isImplements_SuperClassImplementation_ReturnsTrue() {
        // Given
        when(mockClass.getInterfaces()).thenReturn(new PsiClass[0]);
        when(mockClass.getSuperClass()).thenReturn(mockSuperClass);
        when(mockSuperClass.getInterfaces()).thenReturn(new PsiClass[]{mockInterface1});
        when(mockInterface1.getQualifiedName()).thenReturn("com.example.TestInterface");
        when(mockInterface1.getName()).thenReturn("TestInterface");

        // When
        final boolean result = classEntry.isImplements("TestInterface");

        // Then
        assertTrue(result);
        verify(mockClass).getInterfaces();
        verify(mockClass).getSuperClass();
        verify(mockSuperClass).getInterfaces();
        verify(mockInterface1).getQualifiedName();
        verify(mockInterface1).getName();
    }

    @Test
    void isImplements_SuperClassIndirectImplementation_ReturnsTrue() {
        // Given
        when(mockClass.getInterfaces()).thenReturn(new PsiClass[0]);
        when(mockClass.getSuperClass()).thenReturn(mockSuperClass);
        when(mockSuperClass.getInterfaces()).thenReturn(new PsiClass[]{mockInterface1});
        when(mockInterface1.getInterfaces()).thenReturn(new PsiClass[]{mockSuperInterface});
        when(mockSuperInterface.getQualifiedName()).thenReturn("com.example.TestInterface");
        when(mockSuperInterface.getName()).thenReturn("TestInterface");

        // When
        final boolean result = classEntry.isImplements("TestInterface");

        // Then
        assertTrue(result);
        verify(mockClass).getInterfaces();
        verify(mockClass).getSuperClass();
        verify(mockSuperClass).getInterfaces();
        verify(mockInterface1).getInterfaces();
        verify(mockSuperInterface).getQualifiedName();
        verify(mockSuperInterface).getName();
    }

    @Test
    void isImplements_NoImplementation_ReturnsFalse() {
        // Given
        when(mockClass.getInterfaces()).thenReturn(new PsiClass[0]);
        when(mockClass.getSuperClass()).thenReturn(null);

        // When
        final boolean result = classEntry.isImplements("TestInterface");

        // Then
        assertFalse(result);
        verify(mockClass).getInterfaces();
        verify(mockClass).getSuperClass();
    }
}
