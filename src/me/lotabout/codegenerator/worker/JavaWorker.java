package me.lotabout.codegenerator.worker;

import com.intellij.codeInsight.generation.GenerateMembersUtil;
import com.intellij.codeInsight.generation.GenerationInfo;
import com.intellij.codeInsight.generation.PsiGenerationInfo;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.JavaProjectRootsUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.refactoring.PackageWrapper;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveClassesOrPackagesUtil;
import com.intellij.util.IncorrectOperationException;
import me.lotabout.codegenerator.ConflictResolutionPolicy;
import me.lotabout.codegenerator.config.CodeTemplate;
import me.lotabout.codegenerator.util.GenerationUtil;
import me.lotabout.codegenerator.util.PackageUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.generate.config.DuplicationPolicy;
import org.jetbrains.java.generate.exception.GenerateCodeException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JavaWorker {
    private static final Logger logger = Logger.getInstance("#" + JavaWorker.class.getName());

    private final Editor editor;
    private final PsiClass clazz;
    private final CodeTemplate codeTemplate;

    public JavaWorker(PsiClass clazz, Editor editor, CodeTemplate codeTemplate) {
        this.clazz = clazz;
        this.editor = editor;
        this.codeTemplate = codeTemplate;
    }

    public void execute(Map<String, Object> context) throws IncorrectOperationException, GenerateCodeException {
        String className = "";
        if (codeTemplate.type.equals("class")) {
            className = GenerationUtil.velocityEvaluate(clazz, context, context, codeTemplate.classNameVm);
            context.put("ClassName", className);
        }

        String body = GenerationUtil.velocityEvaluate(clazz, context, null, codeTemplate.template);
        if (logger.isDebugEnabled()) logger.debug("Method body generated from Velocity:\n" + body);

        switch (codeTemplate.type) {
        case "body":
            executeJavaFile(body);
            break;
        case "class":
            writeToFile(clazz, className, body);
            break;
        }

    }

    private void executeJavaFile(String body) {
        final PsiClass fakeClass;
        try {
            final PsiFile element = PsiFileFactory
                    .getInstance(clazz.getProject()).createFileFromText("filename", JavaFileType.INSTANCE, "class X {" + body + "}");
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

        WriteAction.run(() -> {
            try {
                // delete all members
                membersToDelete.forEach(PsiElement::delete);

                int offset = 0;
                switch (codeTemplate.insertNewMethodOption) {
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

    private ConflictResolutionPolicy handleExistedMember(PsiMember member, PsiMember existingMember) {
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

    private void writeToFile(@NotNull PsiClass selectedClass, String className, String content) {
        Project project = selectedClass.getProject();
        String packageName = ((PsiJavaFile)selectedClass.getContainingFile()).getPackageName();

        Module currentModule = ModuleUtilCore.findModuleForPsiElement(selectedClass);
        assert currentModule != null;

        VirtualFile moduleRoot = ProjectRootManager.getInstance(project).getFileIndex().getSourceRootForFile(selectedClass.getContainingFile().getVirtualFile());
        PsiDirectory moduleRootDir = PsiDirectoryFactory.getInstance(project).createDirectory(moduleRoot);
        PsiDirectory packageDir = PackageUtil.findOrCreateDirectoryForPackage(project, currentModule, packageName, moduleRootDir, true);
        if (packageDir == null) {
            // package is not found or created.
            return;
        }

        final String targetPath = packageDir.getVirtualFile().getPath() + "/" + className + ".java";
        final VirtualFileManager manager = VirtualFileManager.getInstance();
        final VirtualFile virtualFile = manager.refreshAndFindFileByUrl(VfsUtil.pathToUrl(targetPath));
        if (virtualFile != null && virtualFile.exists() && !userConfirmedOverride()) {
            return;
        }

        WriteAction.run(() -> {
            try {
                VirtualFile finalVirtualFile;
                if (virtualFile != null && virtualFile.exists()) {
                    virtualFile.setBinaryContent(content.getBytes(codeTemplate.fileEncoding));
                    finalVirtualFile = virtualFile;
                } else {

                    Path path = Paths.get(targetPath);
                    Files.write(path, content.getBytes(codeTemplate.fileEncoding), StandardOpenOption.CREATE);
                    finalVirtualFile = manager.refreshAndFindFileByUrl(VfsUtil.pathToUrl(targetPath));
                }

                // auto import
                PsiFile file = PsiManager.getInstance(project).findFile(finalVirtualFile);
                Document document = PsiDocumentManager.getInstance(project).getCachedDocument(file);
                PsiDocumentManager.getInstance(project).commitDocument(document);
                if (file instanceof PsiJavaFile) {
                    JavaCodeStyleManager.getInstance(project).shortenClassReferences(file);
                }

                ApplicationManager.getApplication()
                        .invokeLater(() -> FileEditorManager.getInstance(project).openFile(finalVirtualFile, true, true));

            } catch (Exception e) {
                e.printStackTrace();
                GenerationUtil.handleException(project, e);
            }
        });

    }

    private boolean userConfirmedOverride() {
        return Messages.showYesNoDialog("Overwrite?", "File Exists", null) == Messages.OK;
    }
}
