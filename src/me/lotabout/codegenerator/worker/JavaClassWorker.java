package me.lotabout.codegenerator.worker;

import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import me.lotabout.codegenerator.config.CodeTemplate;
import me.lotabout.codegenerator.util.GenerationUtil;
import me.lotabout.codegenerator.util.PackageUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class JavaClassWorker {
    private static final Logger logger = Logger.getInstance(JavaClassWorker.class);

    public static void execute(@NotNull CodeTemplate codeTemplate, @NotNull PsiJavaFile selectedFile, @NotNull
            Map<String, Object> context) {
        final Project project = selectedFile.getProject();

        // fetch necessary parameters

        final String className;
        final String packageName;
        final String FQClass = GenerationUtil.velocityEvaluate(project, context, context, codeTemplate.classNameVm);
        if (logger.isDebugEnabled()) logger.debug("FQClass generated\n" + FQClass);

        int index = FQClass.lastIndexOf(".");
        if (index >= 0) {
            packageName = FQClass.substring(0, index);
            className = FQClass.substring(index+1);
        } else {
            packageName = "";
            className = FQClass;
        }

        context.put("PackageName", packageName);
        context.put("ClassName", className);

        // generate the content of the class

        final String content = GenerationUtil.velocityEvaluate(project, context, null, codeTemplate.template);
        if (logger.isDebugEnabled()) logger.debug("Method body generated from Velocity:\n" + content);

        final String selectedPackage = selectedFile.getPackageName();
        final String targetPackageName = packageName.equals("") ? selectedPackage : packageName;

        // select or create the target package

        final Module currentModule = ModuleUtilCore.findModuleForPsiElement(selectedFile);
        assert currentModule != null;

        final VirtualFile moduleRoot = ProjectRootManager.getInstance(project).getFileIndex().getSourceRootForFile(selectedFile.getVirtualFile());
        assert moduleRoot != null;

        final PsiDirectory moduleRootDir = PsiDirectoryFactory.getInstance(project).createDirectory(moduleRoot);
        final PsiDirectory targetPackageDir = PackageUtil.findOrCreateDirectoryForPackage(
                project, currentModule, targetPackageName, moduleRootDir, codeTemplate.alwaysPromptForPackage);
        if (targetPackageDir == null) {
            // package is not found or created.
            return;
        }

        final String targetPath = targetPackageDir.getVirtualFile().getPath() + File.separator + className + ".java";
        final VirtualFileManager manager = VirtualFileManager.getInstance();
        final VirtualFile virtualFile = manager.refreshAndFindFileByUrl(VfsUtil.pathToUrl(targetPath));
        if (virtualFile != null && virtualFile.exists() && !userConfirmedOverride()) {
            return;
        }


        WriteAction.run(() -> {
            try {
                // write the content to file.

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

                assert finalVirtualFile != null;
                PsiFile file = PsiManager.getInstance(project).findFile(finalVirtualFile);
                assert file != null;
                Document document = PsiDocumentManager.getInstance(project).getCachedDocument(file);

                if (document != null) {
                    PsiDocumentManager.getInstance(project).commitDocument(document);
                }

                if (file instanceof PsiJavaFile) {
                    JavaCodeStyleManager.getInstance(project).shortenClassReferences(file);
                }

                // open the file in editor
                ApplicationManager.getApplication()
                        .invokeLater(() -> FileEditorManager.getInstance(project).openFile(finalVirtualFile, true, true));

            } catch (Exception e) {
                e.printStackTrace();
                GenerationUtil.handleException(project, e);
            }
        });
    }

    private static boolean userConfirmedOverride() {
        return Messages.showYesNoDialog("Overwrite?", "File Exists", null) == Messages.OK;
    }
}
