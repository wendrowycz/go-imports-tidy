package eu.oakroot;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class TidySaveFieListener implements FileDocumentManagerListener {
    private static final Logger LOG = Logger.getInstance(TidySaveFieListener.class);
    @Override
    public void beforeDocumentSaving(@NotNull Document document) {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        for(Project project: projects) {
            GoImportTidy goImportTidy = new GoImportTidy();

            String importsBlockStr = goImportTidy.findImports(document.getText());
            ArrayList<String> importsBlock = new ArrayList<>(Arrays.asList(importsBlockStr.split("\n")));
            String local = TidyProjectSettingsConfigurable.getOptionTextString(project, "localPrefix");
            try {
                ParsedFile parsedFile = goImportTidy.parseFile(importsBlock, local);
                if (parsedFile.isParsed()) {
                    document.setText(document.getText().replaceAll(importsBlockStr, parsedFile.getFileContent()));
                }
            } catch (IOException e) {
                LOG.debug(e.getMessage());
            }
        }
    }
}
