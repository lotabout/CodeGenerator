package me.lotabout.codegenerator.worker;

import com.intellij.codeInsight.generation.GenerateMembersUtil;
import com.intellij.codeInsight.generation.GenerationInfo;
import com.intellij.codeInsight.generation.PsiGenerationInfo;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import me.lotabout.codegenerator.ConflictResolutionPolicy;
import me.lotabout.codegenerator.config.CodeTemplate;
import me.lotabout.codegenerator.util.GenerationUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.generate.config.DuplicationPolicy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JavaBodyWorker {
    private static final Logger logger = Logger.getInstance(JavaBodyWorker.class);

    public static void execute(@NotNull CodeTemplate codeTemplate, @NotNull PsiClass parentClass,
            @NotNull Editor editor, @NotNull Map<String, Object> context) {
        final Project project = parentClass.getProject();
        String body = GenerationUtil.velocityEvaluate(project, context, null, codeTemplate.template);
        if (logger.isDebugEnabled()) logger.debug("Method body generated from Velocity:\n" + body);

        final PsiClass fakeClass;
        try {
            final PsiFile element = PsiFileFactory
                    .getInstance(parentClass.getProject()).createFileFromText("filename", JavaFileType.INSTANCE, "class X {" + body + "}");
            fakeClass = (PsiClass)element.getLastChild();
            CodeStyleManager.getInstance(parentClass.getProject()).reformat(fakeClass);
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
                existingMember = parentClass.findFieldByName(member.getName(), false);
            } else if (member instanceof PsiMethod) {
                existingMember = parentClass.findMethodBySignature((PsiMethod) member, false);
            } else if (member instanceof PsiClass) {
                existingMember = parentClass.findInnerClassByName(member.getName(), false);
            }

            if (!notAskAgain) {
                policy = handleExistedMember(codeTemplate, member, existingMember);
                notAskAgain = policy == ConflictResolutionPolicy.DUPLICATE_ALL || policy == me.lotabout.codegenerator.ConflictResolutionPolicy.REPLACE_ALL;
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

        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                // delete all members
                membersToDelete.forEach(PsiElement::delete);

                int offset = 0;
                switch (codeTemplate.insertNewMethodOption) {
                case AT_CARET:
                    offset = editor.getCaretModel().getOffset();
                    break;
                case AT_THE_END_OF_A_CLASS:
                    offset = parentClass.getTextRange().getEndOffset() - 1;
                }
                GenerateMembersUtil.insertMembersAtOffset(parentClass.getContainingFile(), offset, generationInfoList);
                // auto import
                JavaCodeStyleManager.getInstance(parentClass.getProject()).shortenClassReferences(parentClass.getContainingFile());
            } catch (Exception e) {
                e.printStackTrace();
                GenerationUtil.handleException(parentClass.getProject(), e);
            }
        });
    }

    private static ConflictResolutionPolicy handleExistedMember(@NotNull CodeTemplate codeTemplate, PsiMember member, PsiMember existingMember) {
        final DuplicationPolicy dupPolicy = codeTemplate.whenDuplicatesOption;
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
