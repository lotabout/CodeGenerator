package me.lotabout.codegenerator.util;

import java.io.File;
import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.util.DirectoryChooserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.impl.ProjectRootUtil;
import com.intellij.openapi.roots.ModulePackageIndex;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Query;

// customize my package Util based on Intellij's built-in Package Util
public class PackageUtil {
    private static final Logger LOG = Logger.getInstance(PackageUtil.class);

    @Nullable
    public static PsiDirectory findOrCreateDirectoryForPackage(@NotNull final Project project,
            @Nullable final Module module, final String packageName,
            @Nullable final PsiDirectory baseDir, final boolean alwaysPrompt)
            throws IncorrectOperationException {
        return findOrCreateDirectoryForPackage(project, module, packageName, baseDir, true, alwaysPrompt);
    }

    @Nullable
    public static PsiDirectory findSourceDirectoryByModuleName(@NotNull final Project project,
            @Nullable final String moduleName) {
        return Arrays.stream(ProjectRootUtil.getSourceRootDirectories(project))
                .filter(psiDirectory -> psiDirectory.getVirtualFile().getPath().contains(moduleName))
                .findFirst()
                .orElse(null);
    }


    @Nullable
    public static PsiDirectory findOrCreateDirectoryForPackage(@NotNull final Project project,
            @Nullable final Module module, String packageName, final PsiDirectory baseDir,
            final boolean askUserToCreate, final boolean alwaysPrompt)
            throws IncorrectOperationException {
        PsiDirectory psiDirectory = null;
        if (!alwaysPrompt && !packageName.isEmpty()) {
            PsiPackage rootPackage = findLongestExistingPackage(module, packageName);
            rootPackage = rootPackage == null ? findLongestExistingPackage(project, packageName) : rootPackage;

            if (rootPackage != null) {
                final int beginIndex = rootPackage.getQualifiedName().length() + 1;
                packageName = beginIndex < packageName.length() ? packageName.substring(beginIndex) : "";
                String postfixToShow = packageName.replace('.', File.separatorChar);
                if (!packageName.isEmpty()) {
                    postfixToShow = File.separatorChar + postfixToShow;
                }
                final PsiDirectory[] moduleDirectories = getPackageDirectoriesInModule(rootPackage, module);
                final PsiDirectory initDir = findDirectory(moduleDirectories, baseDir);
                psiDirectory = DirectoryChooserUtil.selectDirectory(project, moduleDirectories, initDir, postfixToShow);
                if (psiDirectory == null) return null;
            }
        }

        if (psiDirectory == null) {
            final PsiDirectory[] sourceDirectories = ProjectRootUtil.getSourceRootDirectories(project);
            final PsiDirectory initDir = findDirectory(sourceDirectories, baseDir);
            psiDirectory = DirectoryChooserUtil.selectDirectory(project, sourceDirectories, initDir,
                    File.separatorChar + packageName.replace('.', File.separatorChar));
            if (psiDirectory == null) return null;
        }

        String restOfName = packageName;
        boolean askedToCreate = false;
        while (!restOfName.isEmpty()) {
            final String name = getLeftPart(restOfName);
            final PsiDirectory foundExistingDirectory = psiDirectory.findSubdirectory(name);
            if (foundExistingDirectory == null) {
                if (!askedToCreate && askUserToCreate) {
                    if (!ApplicationManager.getApplication().isUnitTestMode()) {
                        final int toCreate = Messages.showYesNoDialog(project,
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
                    psiDirectory = WriteAction.compute(() -> psiDirectory1.createSubdirectory(name));
                } catch (final IncorrectOperationException e) {
                    throw e;
                } catch (final Exception e) {
                    LOG.error(e);
                }
            } else {
                psiDirectory = foundExistingDirectory;
            }
            restOfName = cutLeftPart(restOfName);
        }
        return psiDirectory;
    }

    private static PsiDirectory[] getPackageDirectoriesInModule(final PsiPackage rootPackage, final Module module) {
        return rootPackage.getDirectories(GlobalSearchScope.moduleScope(module));
    }

    private static PsiPackage findLongestExistingPackage(final Project project, final String packageName) {
        final PsiManager manager = PsiManager.getInstance(project);
        String nameToMatch = packageName;
        while (true) {
            final PsiPackage aPackage = JavaPsiFacade.getInstance(manager.getProject()).findPackage(nameToMatch);
            if (aPackage != null && isWritablePackage(aPackage)) return aPackage;
            final int lastDotIndex = nameToMatch.lastIndexOf('.');
            if (lastDotIndex >= 0) {
                nameToMatch = nameToMatch.substring(0, lastDotIndex);
            } else {
                return null;
            }
        }
    }

    private static boolean isWritablePackage(final PsiPackage aPackage) {
        final PsiDirectory[] directories = aPackage.getDirectories();
        for (final PsiDirectory directory : directories) {
            if (directory.isValid() && directory.isWritable()) {
                return true;
            }
        }
        return false;
    }

    private static PsiDirectory getWritableModuleDirectory(@NotNull final Query<VirtualFile> vFiles,
        @NotNull final Module module, final PsiManager manager) {
        for (final VirtualFile vFile : vFiles) {
            if (ModuleUtil.findModuleForFile(vFile, module.getProject()) != module) continue;
            final PsiDirectory directory = manager.findDirectory(vFile);
            if (directory != null && directory.isValid() && directory.isWritable()) {
                return directory;
            }
        }
        return null;
    }

    private static PsiPackage findLongestExistingPackage(final Module module, final String packageName) {
        if (module == null) {
            return null;
        }

        final PsiManager manager = PsiManager.getInstance(module.getProject());

        String nameToMatch = packageName;
        while (true) {
            final Query<VirtualFile> vFiles = ModulePackageIndex
                .getInstance(module)
                .getDirsByPackageName(nameToMatch, false);
            final PsiDirectory directory = getWritableModuleDirectory(vFiles, module, manager);
            if (directory != null) return JavaDirectoryService.getInstance().getPackage(directory);

            final int lastDotIndex = nameToMatch.lastIndexOf('.');
            if (lastDotIndex >= 0) {
                nameToMatch = nameToMatch.substring(0, lastDotIndex);
            } else {
                return null;
            }
        }
    }

    private static String getLeftPart(final String packageName) {
        final int index = packageName.indexOf('.');
        return index > -1 ? packageName.substring(0, index) : packageName;
    }

    private static String cutLeftPart(final String packageName) {
        final int index = packageName.indexOf('.');
        return index > -1 ? packageName.substring(index + 1) : "";
    }

    private static PsiDirectory findDirectory(final PsiDirectory[] directories, final PsiDirectory baseDir) {
        final VirtualFile baseFile = baseDir.getVirtualFile();
        for (final PsiDirectory directory : directories) {
            if (directory.getVirtualFile().equals(baseFile)) {
                return directory;
            }
        }
        return null;
    }
}
