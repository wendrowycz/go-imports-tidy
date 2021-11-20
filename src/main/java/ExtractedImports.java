import java.util.List;
import java.util.Map;

public class ExtractedImports {
    private final Map<Integer, List<String>> contents;
    private final Boolean status;

    public ExtractedImports(Map<Integer, List<String>> contents, Boolean status) {
        this.contents = contents;
        this.status = status;
    }

    public Map<Integer, List<String>> getContents() {
        return contents;
    }

    public Boolean getStatus() {
        return status;
    }
}
