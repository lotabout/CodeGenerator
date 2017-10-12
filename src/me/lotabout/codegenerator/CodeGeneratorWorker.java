package me.lotabout.codegenerator;

import com.intellij.codeInsight.generation.GenerateMembersUtil;
import com.intellij.codeInsight.generation.GenerationInfo;
import com.intellij.codeInsight.generation.PsiGenerationInfo;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
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

        final PsiFile element = PsiFileFactory.getInstance(clazz.getProject()).createFileFromText("filename", JavaFileType.INSTANCE, "class X {" + body + "}");
        final PsiClass fakeClass = (PsiClass)element.getLastChild();

        List<GenerationInfo> generationInfoList = new ArrayList<>();
        for (PsiField field : fakeClass.getFields()) {
            generationInfoList.add(new PsiGenerationInfo<>(field, false));
        }
        for (PsiMethod method: fakeClass.getMethods()) {
            generationInfoList.add(new PsiGenerationInfo<>(method, false));
        }
        for (PsiClass clazz: fakeClass.getInnerClasses()) {
            generationInfoList.add(new PsiGenerationInfo<>(clazz, false));
        }


        int offset = (editor != null) ? editor.getCaretModel().getOffset() : (clazz.getTextRange().getEndOffset() - 1);
        GenerateMembersUtil.insertMembersAtOffset(clazz, offset, generationInfoList);
    }
}
