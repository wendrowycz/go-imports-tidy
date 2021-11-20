import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TidyOptionsFactory {
    private static final TidyOptionsFactory instance = new TidyOptionsFactory();
    private final Map<Enum<TYPE>, ArrayList<Map<String, String>>> options;
    private final HashMap<String, HashMap<String, String>> optionsMap;

    public enum TYPE {
        LOCAL
    }

    private TidyOptionsFactory() {
        options = new HashMap<>();
        optionsMap = new HashMap<>();

        addOptionsType();
        addOption();
    }

    private void addOptionsType() {
        options.put(TYPE.LOCAL, new ArrayList<>());
    }

    private void addOption() {
        HashMap<String, String> optionMap = new HashMap<>();
        optionMap.put("description", "Set local prefix");
        optionMap.put("key", "localPrefix");
        optionMap.put("toolTip", "Use: github.com/namespace");
        String optionId = "localPrefix";
        optionMap.put("id", optionId);
        optionsMap.put(optionId, optionMap);
        options.get(TYPE.LOCAL).add(optionMap);
    }

    public static String getOptionId(Enum<TYPE> optionsType, String key) {
        return optionsType + "_" + key;
    }

//    public static Map<Enum<TYPE>, ArrayList<Map<String, String>>> getOptions() {
//        return instance.options;
//    }

    public static HashMap<String, String> getOptionById(String optionId) {
        return instance.optionsMap.get(optionId);
    }
}
