package me.lotabout.codegenerator;

import com.intellij.codeInsight.generation.GenerateMembersUtil;
import com.intellij.codeInsight.generation.GenerationInfo;
import com.intellij.codeInsight.generation.PsiGenerationInfo;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.java.generate.exception.GenerateCodeException;

import java.util.*;

public class CodeGeneratorWorker {
    private static final Logger logger = Logger.getInstance("#" + CodeGeneratorWorker.class.getName());

    private final Editor editor;
    private final PsiClass clazz;

    public CodeGeneratorWorker(PsiClass clazz, Editor editor) {
        this.clazz = clazz;
        this.editor = editor;
    }

    public void execute(Collection<PsiMember> members, String template) throws IncorrectOperationException, GenerateCodeException {
        // user didn't click cancel so go on
        Map<String, String> params = new HashMap<>();

        String body = GenerationUtil.velocityGenerateCode(clazz, members, params, template, 0, false);

        if (logger.isDebugEnabled()) logger.debug("Method body generated from Velocity:\n" + body);

        // fix weird linebreak problem in IDEA #3296 and later
        body = StringUtil.convertLineSeparators(body);

        PsiFile file = clazz.getContainingFile();
        if (file instanceof PsiJavaFile) {
            executeJavaFile(body);
        }
    }

    private void executeJavaFile(String body) {
        final PsiClass fakeClass;
        try {
            final PsiFile element = PsiFileFactory.getInstance(clazz.getProject()).createFileFromText("filename", JavaFileType.INSTANCE, "class X {" + body + "}");
            fakeClass = (PsiClass)element.getLastChild();
            CodeStyleManager.getInstance(clazz.getProject()).reformat(fakeClass);
        } catch (IncorrectOperationException ignore) {
            HintManager.getInstance().showErrorHint(editor, "fail to generate code from template" );
            return;
        }

        // replace all fields (for now) TODO: add policy for asking

        List<GenerationInfo> generationInfoList = new ArrayList<>();
        for (PsiField field : fakeClass.getFields()) {
            PsiField existingField = clazz.findFieldByName(field.getName(), false);
            if (existingField != null) {
                existingField.delete();
            }
            generationInfoList.add(new PsiGenerationInfo<>(field, false));
        }

        for (PsiMethod method: fakeClass.getMethods()) {
            PsiMethod existingMethod = clazz.findMethodBySignature(method, false);
            if (existingMethod != null) {
                existingMethod.delete();
            }
            generationInfoList.add(new PsiGenerationInfo<>(method, false));
        }

        for (PsiClass clazz: fakeClass.getInnerClasses()) {
            PsiClass existingClass = clazz.findInnerClassByName(clazz.getName(), false);
            if (existingClass != null) {
                existingClass.delete();
            }
            generationInfoList.add(new PsiGenerationInfo<>(clazz, false));
        }

        int offset = (editor != null) ? editor.getCaretModel().getOffset() : (clazz.getTextRange().getEndOffset() - 1);
        GenerateMembersUtil.insertMembersAtOffset(clazz, offset, generationInfoList);

        // auto import
        JavaCodeStyleManager.getInstance(clazz.getProject()).shortenClassReferences(clazz.getContainingFile());
    }
}
