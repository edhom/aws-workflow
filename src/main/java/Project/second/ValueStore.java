package Project.second;

import java.util.HashMap;


public class ValueStore {
    public HashMap<String, String> valueStore = new HashMap<>();

    public boolean addValue(String source, String value) {
        if(value.startsWith("\"")&&value.endsWith("\"")){
            value=value.substring(1,value.length()-1);
        }
        System.out.println("Adding " + source + " with value " + value);
        String previous = valueStore.putIfAbsent(source, value);
        if (previous != null) {
            System.err.println("value " + source + " overwritten!!! previous was " + previous);
            return false;
        } else return true;
    }

    public String getValue(String source) {
        if (source.contains("/")) {
            String dataValue = valueStore.get(source);

            return dataValue;
        } else {
            // constant given by workflow
            return source;
        }

    }

}

