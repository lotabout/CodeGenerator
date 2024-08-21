package me.lotabout.codegenerator.worker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.generate.config.DuplicationPolicy;

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
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.util.IncorrectOperationException;

import me.lotabout.codegenerator.ConflictResolutionPolicy;
import me.lotabout.codegenerator.config.CodeTemplate;
import me.lotabout.codegenerator.config.include.Include;
import me.lotabout.codegenerator.util.GenerationUtil;

import static com.intellij.codeInsight.generation.GenerateMembersUtil.insertMembersAtOffset;

import static me.lotabout.codegenerator.util.GenerationUtil.velocityEvaluate;

public class JavaBodyWorker {
    private static final Logger logger = Logger.getInstance(JavaBodyWorker.class);

    public static void execute(@NotNull final CodeTemplate codeTemplate,
            final List<Include> includes, @NotNull final PsiClass parentClass,
            @NotNull final Editor editor, @NotNull final Map<String, Object> context) {
        final Project project = parentClass.getProject();
        final String body = velocityEvaluate(project, context, null, codeTemplate.template, includes);
        logger.debug("Method body generated from Velocity:\n", body);

        final PsiClass fakeClass;
        try {
            final PsiFile element = PsiFileFactory
                    .getInstance(parentClass.getProject())
                    .createFileFromText("filename", JavaFileType.INSTANCE, "class X {" + body + "}");
            fakeClass = (PsiClass) element.getLastChild();
            CodeStyleManager.getInstance(parentClass.getProject()).reformat(fakeClass);
        } catch (final IncorrectOperationException ignore) {
            HintManager.getInstance().showErrorHint(editor, "fail to generate code from template");
            return;
        }

        final List<GenerationInfo> generationInfoList = new ArrayList<>();
        final List<PsiMember> membersToDelete = new ArrayList<>();

        final List<PsiMember> allMembers = new ArrayList<>();
        allMembers.addAll(Arrays.asList(fakeClass.getFields()));
        allMembers.addAll(Arrays.asList(fakeClass.getMethods()));
        allMembers.addAll(Arrays.asList(fakeClass.getInnerClasses()));

        boolean askAgain = true;
        ConflictResolutionPolicy policy = ConflictResolutionPolicy.DUPLICATE;
        for (final PsiMember member : allMembers) {
            PsiMember existingMember = null;
            if (member instanceof PsiField) {
                existingMember = parentClass.findFieldByName(member.getName(), false);
            } else if (member instanceof PsiMethod) {
                existingMember = parentClass.findMethodBySignature((PsiMethod) member, false);
            } else if (member instanceof PsiClass) {
                existingMember = parentClass.findInnerClassByName(member.getName(), false);
            }
            if (askAgain) {
                policy = handleExistedMember(codeTemplate, member, existingMember);
                askAgain = (policy != ConflictResolutionPolicy.DUPLICATE_ALL)
                    && (policy != ConflictResolutionPolicy.REPLACE_ALL);
            }
            switch (policy) {
                case CANCEL:
                    return;
                case REPLACE:
                case REPLACE_ALL:
                    if (existingMember != null) {
                        membersToDelete.add(existingMember);
                    }
                    break;
            }
            generationInfoList.add(new PsiGenerationInfo<>(member, false));
        }

        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                // delete all members
                membersToDelete.forEach(PsiElement::delete);

                final int offset = switch (codeTemplate.insertNewMethodOption) {
                  case AT_CARET -> editor.getCaretModel().getOffset();
                  case AT_THE_END_OF_A_CLASS -> parentClass.getTextRange().getEndOffset() - 1;
                  default -> 0;
                };
                insertMembersAtOffset(parentClass.getContainingFile(), offset, generationInfoList);
                // auto import
                JavaCodeStyleManager
                    .getInstance(parentClass.getProject())
                    .shortenClassReferences(parentClass.getContainingFile());
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
                GenerationUtil.handleException(parentClass.getProject(), e);
            }
        });
    }

    private static ConflictResolutionPolicy handleExistedMember(@NotNull final CodeTemplate codeTemplate,
            final PsiMember member, final PsiMember existingMember) {
        final DuplicationPolicy dupPolicy = codeTemplate.whenDuplicatesOption;
        if (dupPolicy == DuplicationPolicy.ASK && existingMember != null) {
            final DialogBuilder builder = new DialogBuilder();
            builder.setTitle("Replace existing member: " + member.getName() + "?");
            builder.addOkAction();
            builder.addCancelAction();
            final int exit = Messages.showDialog("Replace existing member: " + member.getName() + "?",
                    "Member Already Exists",
                    new String[]{"Yes for All", "Yes", "Cancel", "No", "No for all"},
                    1, 3,
                    Messages.getQuestionIcon(),
                    null);

          return switch (exit) {
            case 0 -> ConflictResolutionPolicy.REPLACE_ALL;
            case 1 -> ConflictResolutionPolicy.REPLACE;
            case 2 -> ConflictResolutionPolicy.CANCEL;
            case 3 -> ConflictResolutionPolicy.DUPLICATE;
            case 4 -> ConflictResolutionPolicy.DUPLICATE_ALL;
            default -> ConflictResolutionPolicy.DUPLICATE;
          };
        } else if (dupPolicy == DuplicationPolicy.REPLACE) {
            return ConflictResolutionPolicy.REPLACE;
        }

        // If there is no conflict, duplicate policy will do the trick
        return ConflictResolutionPolicy.DUPLICATE;
    }
}
