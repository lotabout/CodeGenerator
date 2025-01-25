package me.lotabout.codegenerator.worker;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;

import me.lotabout.codegenerator.config.CodeTemplate;
import me.lotabout.codegenerator.config.include.Include;

import static me.lotabout.codegenerator.util.GenerationUtil.velocityEvaluate;

public class JavaCaretWorker {
    private static final Logger logger = Logger.getInstance(JavaCaretWorker.class);

    public static void execute(@NotNull final CodeTemplate codeTemplate,
            final List<Include> includes, @NotNull final PsiFile file,
            @NotNull final Editor editor, @NotNull final Map<String, Object> context) {
        final Project project = file.getProject();
        final String content = velocityEvaluate(project, context, null, codeTemplate.template, includes);
        logger.debug("Method body generated from Velocity:\n{}", content);
        //Access document, caret, and selection
        final Document document = editor.getDocument();
        final SelectionModel selectionModel = editor.getSelectionModel();

        final int start = selectionModel.getSelectionStart();
        final int end = selectionModel.getSelectionEnd();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            document.replaceString(start, end, content);
            PsiDocumentManager.getInstance(project).commitDocument(document);
            if (file instanceof PsiJavaFile) {
                final PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
                final PsiClass clazz = PsiTreeUtil.getParentOfType(element, PsiClass.class, false);
                if (clazz == null) {
                    logger.error("Cannot find PsiClass from the current caret position");
                    HintManager
                        .getInstance()
                        .showErrorHint(editor, "Cannot find PsiClass from the current caret position");
                    return;
                }
                JavaCodeStyleManager
                    .getInstance(project)
                    .shortenClassReferences(clazz.getContainingFile());
            }
        });
        selectionModel.removeSelection();
    }
}
