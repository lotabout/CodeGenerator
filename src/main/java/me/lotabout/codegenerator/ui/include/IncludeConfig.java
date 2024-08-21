package me.lotabout.codegenerator.ui.include;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ui.Messages;

import me.lotabout.codegenerator.CodeGeneratorSettings;
import me.lotabout.codegenerator.config.include.Include;
import me.lotabout.codegenerator.config.include.IncludeList;

public class IncludeConfig {
    private JPanel mainPane;
    private JButton addTemplateButton;
    private JSplitPane splitPane;
    private JList<IncludeEditPane> includeList;
    private final DefaultListModel<IncludeEditPane> includeListModel;
    private JButton deleteButton;
    private JPanel splitRightPane;
    private JScrollPane scrollPane;
    private JButton importButton;
    private JButton exportButton;
    private JButton exportAllButton;

    private static final String DEFAULT_EXPORT_PATH = "template.xml";

    public IncludeConfig(final CodeGeneratorSettings settings) {
        this.includeListModel = new DefaultListModel<>();
        this.includeList.setModel(includeListModel);

        includeList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }

            final var length = includeListModel.getSize();
            final var index = includeList.getSelectedIndex();
            if (length < 0 || index < 0 || index >= length) {
                splitPane.setRightComponent(splitRightPane);
                deleteButton.setEnabled(false);
                return;
            }

            final var pane = includeListModel.get(includeList.getSelectedIndex());
            deleteButton.setEnabled(true);
            splitPane.setRightComponent(pane.templateEdit());
        });

        addTemplateButton.addActionListener(e -> {
            final var template = new Include();
            template.name = "Untitled";
            final var editPane = new IncludeEditPane(template);
            final var model = (DefaultListModel<IncludeEditPane>) includeList.getModel();
            model.addElement(editPane);
            includeList.setSelectedIndex(model.getSize()-1);
        });

        deleteButton.addActionListener(e -> {
            final var index = includeList.getSelectedIndex();
            final var size = includeListModel.getSize();
            if (index >= 0 && index < size) {
                final var result = Messages.showYesNoDialog("Delete this template?", "Delete", null);
                if (result == Messages.OK) {
                    final var lastIndex = includeList.getAnchorSelectionIndex();
                    includeListModel.remove(index);

                    var nextIndex = -1;
                    if (lastIndex >= 0 && lastIndex < index || lastIndex == index && index < size-1) {
                        nextIndex = lastIndex;
                    } else if (lastIndex == index || lastIndex > index && lastIndex < size-1) {
                        nextIndex = lastIndex - 1;
                    } else if (lastIndex >= index){
                        nextIndex = size-2; // should not be here
                    }
                    includeList.setSelectedIndex(nextIndex);
                }
            }
        });

        exportButton.addActionListener((final ActionEvent e) -> {
            final var index = includeList.getSelectedIndex();
            final var includeModel = includeListModel.get(index);
            final var xml = IncludeList.toXML(includeModel.getInclude());
            saveToFile(xml);
        });

        exportAllButton.addActionListener((final ActionEvent e) -> {
            final List<Include> templates = new ArrayList<>();
            for (var i = 0; i< includeListModel.getSize(); i++) {
                templates.add(includeListModel.get(i).getInclude());
            }
            final var xml = IncludeList.toXML(templates);
            saveToFile(xml);
        });

        importButton.addActionListener(e -> {
            readFromFile().thenAccept(xml -> {
                try {
                    final var templates = IncludeList.fromXML(xml);
                    final var currentTemplates = getIncludes();
                    currentTemplates.addAll(templates);
                    refresh(currentTemplates);
                    Messages.showMessageDialog("Import finished!", "Import", null);
                } catch (final Exception ex) {
                    ex.printStackTrace();
                    Messages.showMessageDialog("Fail to import\n"+ex.getMessage(), "Import Error", null);
                }
            });
        });

        resetTabPane(settings.getIncludes());
    }

    public void refresh(final List<Include> templates) {
        includeListModel.removeAllElements();
        resetTabPane(templates);
    }

    private void saveToFile(final String content) {
        final var descriptor = FileChooserDescriptorFactory.createSingleLocalFileDescriptor();
        descriptor.setTitle("Choose Directory to Export");
        descriptor.setDescription("save to directory/"+DEFAULT_EXPORT_PATH + " or the file to overwrite");
        FileChooser.chooseFile(descriptor, null, mainPane, null, virtualFile -> {
            final String targetPath;
            if (virtualFile.isDirectory()) {
                targetPath = virtualFile.getPath() + '/' + DEFAULT_EXPORT_PATH;
            } else {
                targetPath = virtualFile.getPath();
            }

            final var path = Paths.get(targetPath);
            if (virtualFile.isDirectory() && Files.exists(path)) {
                final var result = Messages.showYesNoDialog("Overwrite the file?\n" + path, "Overwrite", null);
                if (result != Messages.OK) {
                    return;
                }
            }

            try {
                Files.write(path, content.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                Messages.showMessageDialog("Exported to \n"+path, "Export Successful", null);
            } catch (final IOException e) {
                e.printStackTrace();
                Messages.showMessageDialog("Error occurred\n"+e.getMessage(), "Export Error", null);
            }
        });
    }

    private CompletableFuture<String> readFromFile() {
        final var descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("xml");
        descriptor.setTitle("Choose File to Import");
        final var result = new CompletableFuture<String>();
        FileChooser.chooseFile(descriptor, null, mainPane, null, virtualFile -> result.complete(FileDocumentManager.getInstance().getDocument(virtualFile).getText()));
        return result;
    }

    private void resetTabPane(final List<Include> includes) {
        includes.forEach(include -> {
            if (include == null) return;
            final var editPane = new IncludeEditPane(include);
            includeListModel.addElement(editPane);
        });

        // select first item
        includeList.setSelectedIndex(0);
    }

    public List<Include> getIncludes() {
        final List<Include> ret = new ArrayList<>();
        for (var i = 0; i< includeListModel.getSize(); i++) {
            final var value = includeListModel.get(i);
            ret.add(value.getInclude());
        }

        return ret;
    }

    /**
     * Getter method for property <tt>mainPane</tt>.
     *
     * @return property value of mainPane
     */
    public JPanel getMainPane() {
        return mainPane;
    }
}
