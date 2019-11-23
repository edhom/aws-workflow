package Project;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.List;

import static java.lang.StrictMath.abs;

public class f7_OptimalPickUp implements RequestHandler<String, String> {

    @Override
    public String handleRequest(String input, Context context) {
        List<Integer> coordinates = StringToIntegerList.buildIntegerArray(input);

        int x = abs(coordinates.get(0) - coordinates.get(2));
        int y = abs(coordinates.get(1) - coordinates.get(3));

        StringBuilder sb = new StringBuilder(input);
        sb.deleteCharAt(input.length()-1);

        /*if (x + y == 1){
            sb.append(",(wait, 5 minutes))");
        }
        else{
            sb.append(",(pickUp))");
        }*/
        Integer pickUpTime = (x + y);
        sb.append(",(pickUp in " + pickUpTime + " minutes))");

        return sb.toString();
    }
}
