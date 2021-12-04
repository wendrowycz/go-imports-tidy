package eu.oakroot;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public class GoImportTidy extends AnAction {
    public static final int STD_LIB = 0;
    public static final int LOCAL_LIB = 2;
    public static final int EXTERNAL_LIB = 1;

    Comparator<String> lexicalComparator = Comparator.comparing(this::importPath);

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) {
            return;
        }
        final Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            return;
        }
        final Document document = editor.getDocument();
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        if (virtualFile == null) {
            return;
        }

        try {
            String importsBlockStr = findImports(document.getText());
            ArrayList<String> importsBlock = new ArrayList<>(Arrays.asList(importsBlockStr.split("\n")));
            String local = TidyImportsSettingsConfigurable.getOptionTextString(project, TidyImportsOptionsForm.LOCAL_PREFIX);
            ParsedFile parsedFile = parseFile(importsBlock, local);
            if (parsedFile.isParsed()) {
                String newContent = document.getText().replaceAll(importsBlockStr, parsedFile.getFileContent());
                Runnable r = () -> {
                    document.setReadOnly(false);
                    document.setText(newContent);
                };
                WriteCommandAction.runWriteCommandAction(project, r);
            }
        } catch (IOException err) {
            err.printStackTrace();
        }
    }

    public String findImports(String document) {
        String imps = StringUtils.substringBetween(document, "import (", ")");
        if (imps == null) {
            return "";
        }
        return imps;
    }

    public ParsedFile parseFile(List<String> importsBlock, String local) throws IOException {
        ArrayList<String> contents = extractImports(importsBlock);
        if (contents.size() == 0) {
            return new ParsedFile("", false);
        }
        ArrayList<String> imports = formatImports(contents, local);
        String parsed = "\n" + String.join("\n", imports) + "\n";
        return new ParsedFile(parsed, true);
    }

    private @NotNull ArrayList<String> formatImports(@NotNull List<String> imports, String local) {
        ArrayList<String> result = new ArrayList<>();
        boolean needEmptyLine = false;
        Map<Integer, List<String>> groups = new HashMap<>();
        ArrayList<String> stdLib = new ArrayList<>();
        ArrayList<String> locLib = new ArrayList<>();
        ArrayList<String> extLib = new ArrayList<>();

        for (String imp : imports) {
            if (imp.trim().equals("")) {
                continue;
            }
            int gr = group(imp, local);
            switch (gr) {
                case STD_LIB:
                    stdLib.add(imp);
                    break;
                case EXTERNAL_LIB:
                    extLib.add(imp);
                    break;
                case LOCAL_LIB:
                    locLib.add(imp);
                    break;
            }
        }
        groups.put(STD_LIB, stdLib);
        groups.put(EXTERNAL_LIB, extLib);
        groups.put(LOCAL_LIB, locLib);

        for (Map.Entry<Integer, List<String>> group : groups.entrySet()) {
            List<String> groupImports = group.getValue();
            if (groupImports.size() > 0) {
                groupImports.sort(lexicalComparator);
                if (needEmptyLine) {
                    result.add("");
                }
                result.addAll(groupImports);
                needEmptyLine = true;
            }
        }

        return result;
    }

    private int group(String s, String local) {
        String path = importPath(s);
        if (!s.contains(".")) {
            return STD_LIB;
        }
        if (!Objects.equals(path, "") && s.contains(local)) {
            return LOCAL_LIB;
        }
        return EXTERNAL_LIB;
    }

    private @NotNull ArrayList<String> extractImports(@NotNull List<String> importsBlock) {
        ArrayList<String> importSec = new ArrayList<>();
        for (String line : importsBlock) {
            if (!line.contains("\"")) {
                continue;
            }
            importSec.add(line);
        }

        return importSec;
    }

    private @NotNull String importPath(String s) {
        s = s.trim();
        String[] groups = s.split(" ");
        String path = groups[groups.length - 1];

        return unquote(path);
    }

    private static @NotNull String unquote(@NotNull String val) {
        if ((val.startsWith("\"") && val.endsWith("\""))
                || (val.startsWith("'") && val.endsWith("'"))) {
            return val.substring(1, val.length() - 1);
        }

        return val;
    }
}

