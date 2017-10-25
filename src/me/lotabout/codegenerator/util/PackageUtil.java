package me.lotabout.codegenerator.util;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.util.DirectoryChooserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.impl.ProjectRootUtil;
import com.intellij.openapi.roots.ModulePackageIndex;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ActionRunner;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

// customize my package Util based on Intellij's built-in Package Util
public class PackageUtil {
    private static final Logger LOG = Logger.getInstance("me.lotabout.codegenerator.util.PackageUtil");

    @Nullable
    public static PsiDirectory findOrCreateDirectoryForPackage(@NotNull Project project,
            @Nullable Module module,
            String packageName,
            @Nullable PsiDirectory baseDir,
            boolean alwaysPrompt) throws IncorrectOperationException {
        return findOrCreateDirectoryForPackage(project, module, packageName, baseDir, true, alwaysPrompt);
    }

    @Nullable
    public static PsiDirectory findOrCreateDirectoryForPackage(@NotNull Project project,
            @Nullable Module module,
            String packageName,
            PsiDirectory baseDir,
            boolean askUserToCreate,
            boolean alwaysPrompt) throws IncorrectOperationException {
        PsiDirectory psiDirectory = null;
        if (!alwaysPrompt && !packageName.isEmpty()) {
            PsiPackage rootPackage = findLongestExistingPackage(module, packageName);
            rootPackage = rootPackage == null ? findLongestExistingPackage(project, packageName) : rootPackage;

            if (rootPackage != null) {
                int beginIndex = rootPackage.getQualifiedName().length() + 1;
                packageName = beginIndex < packageName.length() ? packageName.substring(beginIndex) : "";
                String postfixToShow = packageName.replace('.', File.separatorChar);
                if (packageName.length() > 0) {
                    postfixToShow = File.separatorChar + postfixToShow;
                }
                PsiDirectory[] moduleDirectories = getPackageDirectoriesInModule(rootPackage, module);
                PsiDirectory initDir = findDirectory(moduleDirectories, baseDir);
                psiDirectory = DirectoryChooserUtil.selectDirectory(project, moduleDirectories, initDir, postfixToShow);
                if (psiDirectory == null) return null;
            }
        }

        if (psiDirectory == null) {
            PsiDirectory[] sourceDirectories = ProjectRootUtil.getSourceRootDirectories(project);
            PsiDirectory initDir = findDirectory(sourceDirectories, baseDir);
            psiDirectory = DirectoryChooserUtil.selectDirectory(project, sourceDirectories, initDir,
                    File.separatorChar + packageName.replace('.', File.separatorChar));
            if (psiDirectory == null) return null;
        }

        String restOfName = packageName;
        boolean askedToCreate = false;
        while (restOfName.length() > 0) {
            final String name = getLeftPart(restOfName);
            PsiDirectory foundExistingDirectory = psiDirectory.findSubdirectory(name);
            if (foundExistingDirectory == null) {
                if (!askedToCreate && askUserToCreate) {
                    if (!ApplicationManager.getApplication().isUnitTestMode()) {
                        int toCreate = Messages.showYesNoDialog(project,
                                IdeBundle.message("prompt.create.non.existing.package", packageName),
                                IdeBundle.message("title.package.not.found"),
                                Messages.getQuestionIcon());
                        if (toCreate != Messages.YES) {
                            return null;
                        }
                    }
                    askedToCreate = true;
                }

                final PsiDirectory psiDirectory1 = psiDirectory;
                try {
                    psiDirectory = ActionRunner.runInsideWriteAction(new ActionRunner.InterruptibleRunnableWithResult<PsiDirectory>() {
                        public PsiDirectory run() throws Exception {
                            return psiDirectory1.createSubdirectory(name);
                        }
                    });
                }
                catch (IncorrectOperationException e) {
                    throw e;
                }
                catch (IOException e) {
                    throw new IncorrectOperationException(e);
                }
                catch (Exception e) {
                    LOG.error(e);
                }
            }
            else {
                psiDirectory = foundExistingDirectory;
            }
            restOfName = cutLeftPart(restOfName);
        }
        return psiDirectory;
    }

    private static PsiDirectory[] getPackageDirectoriesInModule(PsiPackage rootPackage, Module module) {
        return rootPackage.getDirectories(GlobalSearchScope.moduleScope(module));
    }

    private static PsiPackage findLongestExistingPackage(Project project, String packageName) {
        PsiManager manager = PsiManager.getInstance(project);
        String nameToMatch = packageName;
        while (true) {
            PsiPackage aPackage = JavaPsiFacade.getInstance(manager.getProject()).findPackage(nameToMatch);
            if (aPackage != null && isWritablePackage(aPackage)) return aPackage;
            int lastDotIndex = nameToMatch.lastIndexOf('.');
            if (lastDotIndex >= 0) {
                nameToMatch = nameToMatch.substring(0, lastDotIndex);
            }
            else {
                return null;
            }
        }
    }

    private static boolean isWritablePackage(PsiPackage aPackage) {
        PsiDirectory[] directories = aPackage.getDirectories();
        for (PsiDirectory directory : directories) {
            if (directory.isValid() && directory.isWritable()) {
                return true;
            }
        }
        return false;
    }

    private static PsiDirectory getWritableModuleDirectory(@NotNull Query<VirtualFile> vFiles, @NotNull Module module, PsiManager manager) {
        for (VirtualFile vFile : vFiles) {
            if (ModuleUtil.findModuleForFile(vFile, module.getProject()) != module) continue;
            PsiDirectory directory = manager.findDirectory(vFile);
            if (directory != null && directory.isValid() && directory.isWritable()) {
                return directory;
            }
        }
        return null;
    }

    private static PsiPackage findLongestExistingPackage(Module module, String packageName) {
        if (module == null) {
            return null;
        }

        final PsiManager manager = PsiManager.getInstance(module.getProject());

        String nameToMatch = packageName;
        while (true) {
            Query<VirtualFile> vFiles = ModulePackageIndex.getInstance(module).getDirsByPackageName(nameToMatch, false);
            PsiDirectory directory = getWritableModuleDirectory(vFiles, module, manager);
            if (directory != null) return JavaDirectoryService.getInstance().getPackage(directory);

            int lastDotIndex = nameToMatch.lastIndexOf('.');
            if (lastDotIndex >= 0) {
                nameToMatch = nameToMatch.substring(0, lastDotIndex);
            }
            else {
                return null;
            }
        }
    }

    private static String getLeftPart(String packageName) {
        int index = packageName.indexOf('.');
        return index > -1 ? packageName.substring(0, index) : packageName;
    }

    private static String cutLeftPart(String packageName) {
        int index = packageName.indexOf('.');
        return index > -1 ? packageName.substring(index + 1) : "";
    }

    private static PsiDirectory findDirectory(PsiDirectory[] directories, PsiDirectory baseDir) {
        final VirtualFile baseFile = baseDir.getVirtualFile();
        for (PsiDirectory directory : directories) {
            if (directory.getVirtualFile().equals(baseFile)) {
                return directory;
            }
        }
        return null;
    }
}

