package me.lotabout.codegenerator.worker;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.impl.file.PsiDirectoryImpl;

import me.lotabout.codegenerator.config.CodeTemplate;
import me.lotabout.codegenerator.config.include.Include;
import me.lotabout.codegenerator.util.GenerationUtil;

import static me.lotabout.codegenerator.util.PackageUtil.findOrCreateDirectoryForPackage;
import static me.lotabout.codegenerator.util.PackageUtil.findSourceDirectoryByModuleName;

public class JavaClassWorker {
    private static final Logger logger = Logger.getInstance(JavaClassWorker.class);

    public static void execute(@NotNull final CodeTemplate codeTemplate,
            @NotNull final List<Include> includes,
            @NotNull final PsiJavaFile selectedFile,
            @NotNull final Map<String, Object> context) {
        try {
            final Project project = selectedFile.getProject();
            // fetch necessary parameters
            final String className;
            final String packageName;
            final String FQClass = GenerationUtil.velocityEvaluate(project,
                context, context, codeTemplate.classNameVm, includes);
            if (logger.isDebugEnabled()) logger.debug("FQClass generated\n" + FQClass);

            final int index = FQClass.lastIndexOf(".");
            if (index >= 0) {
                packageName = FQClass.substring(0, index);
                className = FQClass.substring(index + 1);
            } else {
                packageName = "";
                className = FQClass;
            }

            context.put("PackageName", packageName);
            context.put("ClassName", className);

            // generate the content of the class

            final String content = GenerationUtil.velocityEvaluate(project, context, null, codeTemplate.template, includes);
            if (logger.isDebugEnabled()) logger.debug("Method body generated from Velocity:\n" + content);

            final String selectedPackage = selectedFile.getPackageName();
            final String targetPackageName = packageName.equals("") ? selectedPackage : packageName;

            // select or create the target package
            final Module currentModule = ModuleUtilCore.findModuleForPsiElement(selectedFile);
            assert currentModule != null;

            final VirtualFile moduleRoot = ProjectRootManager
                .getInstance(project)
                .getFileIndex()
                .getSourceRootForFile(selectedFile.getVirtualFile());
            assert moduleRoot != null;

            final PsiDirectory moduleRootDir = PsiDirectoryFactory
                .getInstance(project)
                .createDirectory(moduleRoot);
            final VirtualFileManager manager = VirtualFileManager.getInstance();

            final PsiDirectory targetPackageDir;
            if (StringUtils.isNotBlank(codeTemplate.defaultTargetModule)) {
                targetPackageDir = findSourceDirectoryByModuleName(project,
                    codeTemplate.defaultTargetModule);
            } else {
                targetPackageDir = findOrCreateDirectoryForPackage(project,
                    currentModule, targetPackageName, moduleRootDir,
                    codeTemplate.alwaysPromptForPackage);
            }

            if (targetPackageDir == null) {
                // package is not found or created.
                return;
            }

            final String targetFileName = className + ".java";
            final String targetPath;
            final String targetDirectory;
            if (StringUtils.isNotBlank(codeTemplate.defaultTargetPackage)) {
                final String subDirectoryPath = codeTemplate.defaultTargetPackage.replace(".", File.separator);
                targetDirectory = targetPackageDir.getVirtualFile().getPath() + File.separator + subDirectoryPath;
                targetPath = targetDirectory + File.separator + targetFileName;
            } else {
                targetDirectory = targetPackageDir.getVirtualFile().getPath();
                targetPath = targetPackageDir.getVirtualFile().getPath() + File.separator + targetFileName;
            }

            final VirtualFile targetFileVf = manager.refreshAndFindFileByUrl(VfsUtil.pathToUrl(targetPath));
            if (targetFileVf != null && targetFileVf.exists() && !userConfirmedOverride()) {
                return;
            }

            final VirtualFile targetDirectoryVf = manager.refreshAndFindFileByUrl(VfsUtil.pathToUrl(targetDirectory));
            if (targetDirectoryVf == null) {
                Messages.showErrorDialog("Failed to create directory: " + targetDirectory, "Error");
                return;
            }
            final PsiDirectory targetPsiDirectory = new PsiDirectoryImpl(new PsiManagerImpl(project), targetDirectoryVf);

            final PsiFile targetFile = PsiFileFactory
                .getInstance(project)
                .createFileFromText(className + ".java", JavaFileType.INSTANCE, content);
            JavaCodeStyleManager.getInstance(project).shortenClassReferences(targetFile);
            CodeStyleManager.getInstance(project).reformat(targetFile);

            WriteCommandAction.runWriteCommandAction(project, () -> {
                try {
                    final PsiFile oldFile = targetPsiDirectory.findFile(targetFileName);
                    if (oldFile != null) {
                        oldFile.delete();
                    }

                    targetPsiDirectory.add(targetFile);
                    final PsiFile addedFile = targetPsiDirectory.findFile(targetFile.getName());
                    if (addedFile == null) {
                        throw new RuntimeException("Failed to create file: " + targetFile.getName());
                    }
                    // open the file in editor
                    ApplicationManager.getApplication()
                            .invokeLater(() -> FileEditorManager
                                .getInstance(project)
                                .openFile(addedFile.getVirtualFile(), true, true));
                } catch (final Exception e) {
                    logger.error("Failed to create file: {}", e.getMessage());
                    GenerationUtil.handleException(project, e);
                }
            });
        } catch (final Exception e){
            logger.error("Failed to generate code: " + e.getMessage(), e);
        }
    }

    private static boolean userConfirmedOverride() {
        return Messages.showYesNoDialog("Overwrite?", "File Exists", null) == Messages.YES;
    }
}
