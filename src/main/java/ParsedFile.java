public class ParsedFile {
    private final String fileContent;
    private final Boolean isParsed;

    public ParsedFile(String fileContent, Boolean status) {
        this.fileContent = fileContent;
        this.isParsed = status;
    }

    public String getFileContent() {
        return fileContent;
    }

    public Boolean isParsed() {
        return isParsed;
    }
}
