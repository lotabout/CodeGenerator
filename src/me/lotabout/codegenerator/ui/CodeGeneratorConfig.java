package me.lotabout.codegenerator.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ui.Messages;
import me.lotabout.codegenerator.CodeGeneratorSettings;
import me.lotabout.codegenerator.config.CodeTemplate;
import me.lotabout.codegenerator.config.CodeTemplateList;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.concurrency.Promise;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class CodeGeneratorConfig {
    private JPanel mainPane;
    private JButton addTemplateButton;
    private JSplitPane splitPane;
    private JList<TemplateEditPane> templateList;
    private DefaultListModel<TemplateEditPane> templateListModel;
    private JButton deleteTemplateButton;
    private JPanel splitRightPane;
    private JScrollPane scrollPane;
    private JButton importButton;
    private JButton exportButton;
    private JButton exportAllButton;

    private static String DEFAULT_EXPORT_PATH = "code-generator.xml";

    public CodeGeneratorConfig(CodeGeneratorSettings settings) {
        this.templateListModel = new DefaultListModel<>();
        this.templateList.setModel(templateListModel);

        templateList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }

            int length = templateListModel.getSize();
            int index = templateList.getSelectedIndex();
            if (length < 0 || index < 0 || index >= length) {
                splitPane.setRightComponent(splitRightPane);
                deleteTemplateButton.setEnabled(false);
                return;
            }

            TemplateEditPane pane = templateListModel.get(templateList.getSelectedIndex());
            deleteTemplateButton.setEnabled(true);
            splitPane.setRightComponent(pane.templateEdit());
        });

        addTemplateButton.addActionListener(e -> {
            CodeTemplate template = new CodeTemplate();
            template.name = "Untitled";
            TemplateEditPane editPane = new TemplateEditPane(template);
            DefaultListModel<TemplateEditPane> model = (DefaultListModel<TemplateEditPane>) templateList.getModel();
            model.addElement(editPane);
            templateList.setSelectedIndex(model.getSize()-1);
        });

        deleteTemplateButton.addActionListener(e -> {
            int index = templateList.getSelectedIndex();
            int size = templateListModel.getSize();
            if (index >= 0 && index < size) {
                int result = Messages.showYesNoDialog("Delete this template?", "Delete", null);
                if (result == Messages.OK) {
                    int lastIndex = templateList.getAnchorSelectionIndex();
                    templateListModel.remove(index);

                    int nextIndex = -1;
                    if (lastIndex >= 0 && lastIndex < index || lastIndex == index && index < size-1) {
                        nextIndex = lastIndex;
                    } else if (lastIndex == index || lastIndex > index && lastIndex < size-1) {
                        nextIndex = lastIndex - 1;
                    } else if (lastIndex >= index){
                        nextIndex = size-2; // should not be here
                    }
                    templateList.setSelectedIndex(nextIndex);
                }
            }
        });

        exportButton.addActionListener((ActionEvent e) -> {
            int index = templateList.getSelectedIndex();
            TemplateEditPane template = templateListModel.get(index);

            String xml = CodeTemplateList.toXML(template.getCodeTemplate());
            saveToFile(xml);
        });

        exportAllButton.addActionListener((ActionEvent e) -> {
            List<CodeTemplate> templates = new ArrayList<>();
            for (int i=0; i<templateListModel.getSize(); i++) {
                templates.add(templateListModel.get(i).getCodeTemplate());
            }

            String xml = CodeTemplateList.toXML(templates);
            saveToFile(xml);
        });

        importButton.addActionListener(e -> {
            readFromFile().done(xml -> {
                try {
                    List<CodeTemplate> templates = CodeTemplateList.fromXML(xml);
                    List<CodeTemplate> currentTemplates = getTabTemplates();
                    currentTemplates.addAll(templates);
                    refresh(currentTemplates);
                    Messages.showMessageDialog("Import finished!", "Import", null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Messages.showMessageDialog("Fail to import\n"+ex.getMessage(), "Import Error", null);
                }
            });
        });

        resetTabPane(settings.getCodeTemplates());
    }

    public void refresh(List<CodeTemplate> templates) {
        templateListModel.removeAllElements();
        resetTabPane(templates);
    }

    private void saveToFile(String content) {
        final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleLocalFileDescriptor();
        descriptor.setTitle("Choose Directory to Export");
        descriptor.setDescription("save to directory/"+DEFAULT_EXPORT_PATH + " or the file to overwrite");
        FileChooser.chooseFile(descriptor, null, mainPane, null, virtualFile -> {
            String targetPath;
            if (virtualFile.isDirectory()) {
                targetPath = virtualFile.getPath() + '/' + DEFAULT_EXPORT_PATH;
            } else {
                targetPath = virtualFile.getPath();
            }

            Path path = Paths.get(targetPath);
            if (virtualFile.isDirectory() && Files.exists(path)) {
                int result = Messages.showYesNoDialog("Overwrite the file?\n" + path, "Overwrite", null);
                if (result != Messages.OK) {
                    return;
                }
            }

            try {
                Files.write(path, content.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                Messages.showMessageDialog("Exported to \n"+path, "Export Successful", null);
            } catch (IOException e) {
                e.printStackTrace();
                Messages.showMessageDialog("Error occurred\n"+e.getMessage(), "Export Error", null);
            }
        });
    }

    private Promise<String> readFromFile() {
        final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("xml");
        descriptor.setTitle("Choose File to Import");
        final AsyncPromise<String> result = new AsyncPromise<>();
        FileChooser.chooseFile(descriptor, null, mainPane, null, virtualFile -> {
            result.setResult(FileDocumentManager.getInstance().getDocument(virtualFile).getText());
        });
        return result;
    }

    private void resetTabPane(List<CodeTemplate> templates) {
        templates.forEach(template -> {
            if (template == null) return;
            TemplateEditPane editPane = new TemplateEditPane(template);
            templateListModel.addElement(editPane);
        });

        // select first item
        templateList.setSelectedIndex(0);
    }

    public List<CodeTemplate> getTabTemplates() {
        List<CodeTemplate> ret = new ArrayList<>();
        for (int i=0; i<templateListModel.getSize(); i++) {
            TemplateEditPane value = templateListModel.get(i);
            ret.add(value.getCodeTemplate());
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
