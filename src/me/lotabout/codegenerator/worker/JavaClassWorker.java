package me.lotabout.codegenerator.worker;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.JavaProjectRootsUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.refactoring.PackageWrapper;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveClassesOrPackagesUtil;
import com.intellij.util.IncorrectOperationException;
import me.lotabout.codegenerator.config.CodeTemplate;
import me.lotabout.codegenerator.util.EntryFactory;
import me.lotabout.codegenerator.util.GenerationUtil;
import org.jetbrains.java.generate.exception.GenerateCodeException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class JavaClassWorker {
    private static final Logger logger = Logger.getInstance("#" + JavaBodyWorker.class.getName());

    private final Editor editor;
    private final PsiClass clazz;
    private final CodeTemplate codeTemplate;

    public JavaClassWorker(PsiClass clazz, Editor editor, CodeTemplate codeTemplate) {
        this.clazz = clazz;
        this.editor = editor;
        this.codeTemplate = codeTemplate;
    }

    public void execute(List<PsiClass> selectedClasses) throws IncorrectOperationException, GenerateCodeException {
        if (selectedClasses.size() <= 0 || codeTemplate.template == null || codeTemplate.classNameVm == null) {
            return;
        }

        Map<String, Object> context = new HashMap<>();

        logger.debug("Velocity Context - adding classes");
        for (int i=0; i<selectedClasses.size(); i++) {
            context.put("class"+String.valueOf(i), EntryFactory.newClassEntry(selectedClasses.get(i)));
        }

        try {

            String className = GenerationUtil.velocityEvaluate(clazz, null, context, codeTemplate.classNameVm);
            context.put("ClassName", className);

            String content = GenerationUtil.velocityEvaluate(clazz, null, context, codeTemplate.template);
            if (logger.isDebugEnabled()) logger.debug("class content generated from Velocity:\n" + content);

            writeToFile(clazz, className, content);
        } catch (GenerateCodeException e) {
            Messages.showMessageDialog(selectedClasses.iterator().next().getProject(), e.getMessage(), "Generate Failed", null);
        }
    }

    private void writeToFile(PsiClass clazz, String className, String content) {
        Project project = clazz.getProject();
        String packageName = ((PsiJavaFile)clazz.getContainingFile()).getPackageName();
        VirtualFile sourceRoot = findSourceRoot(packageName, clazz);
        if (sourceRoot == null) {
            return;
        }

        final String sourcePath = sourceRoot.getPath() + "/" + packageName.replace(".", "/");
        final String targetPath = sourcePath + "/" + className + ".java";
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
                    JavaCodeStyleManager.getInstance(clazz.getProject()).shortenClassReferences(file);
                }

                ApplicationManager.getApplication()
                        .invokeLater(() -> FileEditorManager.getInstance(project).openFile(finalVirtualFile, true, true));

            } catch (Exception e) {
                e.printStackTrace();
                GenerationUtil.handleException(clazz.getProject(), e);
            }
        });

    }


    private VirtualFile findSourceRoot(String packageName, PsiClass clazz) {
        Project project = clazz.getProject();
        final PackageWrapper targetPackage = new PackageWrapper(PsiManager.getInstance(project), packageName);
        List<VirtualFile> suitableRoots = JavaProjectRootsUtil.getSuitableDestinationSourceRoots(project);
        if (suitableRoots.size() > 1) {
            return MoveClassesOrPackagesUtil.chooseSourceRoot(targetPackage, suitableRoots,
                    clazz.getContainingFile().getContainingDirectory());

        } else if (suitableRoots.size() == 1) {
            return suitableRoots.get(0);
        }
        return null;
    }

    private boolean userConfirmedOverride() {
        return Messages.showYesNoDialog("Overwrite?", "File Exists", null) == Messages.OK;
    }
}
