package Project;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringToIntegerList {

    /**
     *
     * @param string for coordinates
     * @return List of Integers with coordinates
     */
    public static List<Integer> buildIntegerArray(String string){

        List<Integer> intArray = new ArrayList<>();
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(string);
        while(m.find()) {
            //System.out.println(m.group());
            intArray.add(Integer.parseInt(m.group()));
        }
        return intArray;
    }
}
