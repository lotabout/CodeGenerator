package me.lotabout.codegenerator;

import com.intellij.codeInsight.generation.GenerateMembersUtil;
import com.intellij.codeInsight.generation.GenerationInfo;
import com.intellij.codeInsight.generation.PsiGenerationInfo;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import me.lotabout.codegenerator.config.GeneratorConfig;
import org.jetbrains.java.generate.config.*;
import org.jetbrains.java.generate.exception.GenerateCodeException;

import java.util.*;

public class CodeGeneratorWorker {
    private static final Logger logger = Logger.getInstance("#" + CodeGeneratorWorker.class.getName());

    private final Editor editor;
    private final PsiClass clazz;
    private final GeneratorConfig generatorConfig;

    public CodeGeneratorWorker(PsiClass clazz, Editor editor, GeneratorConfig generatorConfig) {
        this.clazz = clazz;
        this.editor = editor;
        this.generatorConfig = generatorConfig;
    }

    public void execute(Collection<PsiMember> members, String template) throws IncorrectOperationException, GenerateCodeException {
        // user didn't click cancel so go on
        Map<String, String> params = new HashMap<>();

        String body = GenerationUtil.velocityGenerateCode(clazz, members, params, template, generatorConfig.sortElements, generatorConfig.useFullyQualifiedName);

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

        List<GenerationInfo> generationInfoList = new ArrayList<>();
        List<PsiMember> membersToDelete = new ArrayList<>();

        List<PsiMember> allMembers = new ArrayList<>();
        allMembers.addAll(Arrays.asList(fakeClass.getFields()));
        allMembers.addAll(Arrays.asList(fakeClass.getMethods()));
        allMembers.addAll(Arrays.asList(fakeClass.getInnerClasses()));

        boolean notAskAgain = false;
        ConflictResolutionPolicy policy = ConflictResolutionPolicy.DUPLICATE;
        for (PsiMember member: allMembers) {
            PsiMember existingMember = null;
            if (member instanceof PsiField) {
                existingMember = clazz.findFieldByName(member.getName(), false);
            } else if (member instanceof PsiMethod) {
                existingMember = clazz.findMethodBySignature((PsiMethod) member, false);
            } else if (member instanceof PsiClass) {
                existingMember = clazz.findInnerClassByName(member.getName(), false);
            }

            if (!notAskAgain) {
                policy = handleExistedMember(member, existingMember);
                notAskAgain = policy == ConflictResolutionPolicy.DUPLICATE_ALL || policy == ConflictResolutionPolicy.REPLACE_ALL;
            }

            switch (policy) {
                case CANCEL:
                    return;
                case REPLACE:
                case REPLACE_ALL:
                    membersToDelete.add(existingMember);
                    break;
            }
            generationInfoList.add(new PsiGenerationInfo<>(member, false));
        }

        WriteAction.run(() -> {
            try {
                // delete all members
                membersToDelete.forEach(PsiElement::delete);

                int offset = 0;
                switch (generatorConfig.insertNewMethodOption) {
                    case AT_CARET:
                        offset = (editor != null) ? editor.getCaretModel().getOffset() : (clazz.getTextRange().getEndOffset() - 1);
                        break;
                    case AT_THE_END_OF_A_CLASS:
                        offset = clazz.getTextRange().getEndOffset() - 1;
                }
                GenerateMembersUtil.insertMembersAtOffset(clazz, offset, generationInfoList);
                // auto import
                JavaCodeStyleManager.getInstance(clazz.getProject()).shortenClassReferences(clazz.getContainingFile());
            } catch (Exception e) {
                e.printStackTrace();
                GenerationUtil.handleException(clazz.getProject(), e);
            }
        });
    }

    /**
     * Properly handle the existed member according to the policy
     * For example, delete the existing member in REPLACE policy
     * @param member
     * @param existingMember
     * @return The member to insert, null if no need to insert anything
     */
    private ConflictResolutionPolicy handleExistedMember(PsiMember member, PsiMember existingMember) {
        final DuplicationPolicy dupPolicy = generatorConfig.whenDuplicatesOption;
        if (dupPolicy == DuplicationPolicy.ASK && existingMember != null) {
            DialogBuilder builder = new DialogBuilder();
            builder.setTitle("Replace existing member: " + member.getName() + "?");
            builder.addOkAction();
            builder.addCancelAction();

            int exit = Messages.showDialog("Replace existing member: " + member.getName() + "?",
                    "Member Already Exists",
                    new String[] {"Yes for All", "Yes", "Cancel", "No", "No for all"},
                    1, 3,
                    Messages.getQuestionIcon(),
                    null);

            switch (exit) {
                case 0:
                    return ConflictResolutionPolicy.REPLACE_ALL;
                case 1:
                    return ConflictResolutionPolicy.REPLACE;
                case 2:
                    return ConflictResolutionPolicy.CANCEL;
                case 3:
                    return ConflictResolutionPolicy.DUPLICATE;
                case 4:
                    return ConflictResolutionPolicy.DUPLICATE_ALL;
                default:
                    return ConflictResolutionPolicy.DUPLICATE;
            }
        } else if (dupPolicy == DuplicationPolicy.REPLACE) {
            return ConflictResolutionPolicy.REPLACE;
        }

        // If there is no conflict, duplicate policy will do the trick
        return ConflictResolutionPolicy.DUPLICATE;
    }

}
