import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GoImportTidy extends AnAction {
    private static final int PRE_IMPORT = 0;
    private static final int IMPORT_SECTION = 1;
    private static final int POST_IMPORT = 2;
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
            BufferedReader bufferedReader = new BufferedReader(new FileReader(virtualFile.getPath()));

            String local = TidyProjectSettingsConfigurable.getOptionTextString(project, "localPrefix");
            ParsedFile parsedFile = parseFile(bufferedReader, local);
            if (parsedFile.isParsed()) {
                System.out.println(parsedFile.getFileContent());
                Runnable r = () -> {
                    document.setReadOnly(false);
                    document.setText(parsedFile.getFileContent());
                };
                WriteCommandAction.runWriteCommandAction(project, r);
            }
            System.out.println(parsedFile.isParsed());
        } catch (IOException err) {
            err.printStackTrace();
        }
    }

    public ParsedFile parseFile(BufferedReader file, String local) throws IOException {
        ExtractedImports extractedImports = extractImports(file);
        Map<Integer, List<String>> contents = extractedImports.getContents();
        if (!extractedImports.getStatus() || contents.get(IMPORT_SECTION).size() == 0) {
            return new ParsedFile("", false);
        }
        ArrayList<String> imports = formatImports(contents.get(IMPORT_SECTION), local);
        contents.get(IMPORT_SECTION).clear();
        contents.get(IMPORT_SECTION).add("import (");
        contents.get(IMPORT_SECTION).addAll(imports);
        contents.get(IMPORT_SECTION).add(")");

        List<String> results = new ArrayList<>();
        for (Map.Entry<Integer, List<String>> entry : contents.entrySet()) {
            results.addAll(entry.getValue());
        }
        return new ParsedFile(String.join("\n", results), true);
    }

    private ArrayList<String> formatImports(List<String> imports, String local) {
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

    private ExtractedImports extractImports(BufferedReader s) throws IOException {
        ArrayList<String> preImportSec = new ArrayList<>();
        ArrayList<String> importSec = new ArrayList<>();
        ArrayList<String> postImportSec = new ArrayList<>();
        Map<Integer, List<String>> results = new HashMap<>() {{
            put(PRE_IMPORT, preImportSec);
            put(IMPORT_SECTION, importSec);
            put(POST_IMPORT, postImportSec);
        }};

        String line;
        int phase = PRE_IMPORT;

        while ((line = s.readLine()) != null) {
            int newPhase = nextPart(line, phase);
            if (newPhase == phase) {
                if ((!line.contains("\"")) && phase == IMPORT_SECTION) {
                    continue;
                }
                results.get(phase).add(line);
            }
            phase = newPhase;
        }

        return new ExtractedImports(results, phase == POST_IMPORT);
    }

    private int nextPart(String line, int previousPhase) {
        if (previousPhase == PRE_IMPORT && Objects.equals(line, "import (")) {
            return IMPORT_SECTION;
        }
        if (previousPhase == IMPORT_SECTION && Objects.equals(line, ")")) {
            return POST_IMPORT;
        }
        return previousPhase;
    }

    private String importPath(String s) {
        s = s.trim();
        String[] groups = s.split(" ");
        String path = groups[groups.length - 1];

        return unquote(path);
    }

    private static String unquote(String val) {
        if ((val.startsWith("\"") && val.endsWith("\""))
                || (val.startsWith("'") && val.endsWith("'"))) {
            return val.substring(1, val.length() - 1);
        }

        return val;
    }
}

