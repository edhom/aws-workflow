package Project;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.List;

import static java.lang.StrictMath.abs;

public class f3_CalcProfit implements RequestHandler<String, String > {
    @Override
    public String handleRequest(String input, Context context) {
        List<Integer> coordinates = StringToIntegerList.buildIntegerArray(input);

        Double pricePerKilometer = 2.00;

        int x = abs(coordinates.get(0) - coordinates.get(2));
        int y = abs(coordinates.get(1) - coordinates.get(3));

        Double totalPrice = (x + y) * pricePerKilometer;

        StringBuilder sb = new StringBuilder(input);

        sb.deleteCharAt(input.length()-1);
        sb.append(",(" + totalPrice + "))");

        return sb.toString();
    }
}
