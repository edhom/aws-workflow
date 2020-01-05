package Project;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.json.simple.JSONArray;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.util.Map;

import static java.lang.Math.abs;


public class f3_CalcProfit implements RequestHandler<JSONObject, JSONObject> {
    @Override
    public JSONObject handleRequest(JSONObject input, Context context) {

        Double pricePerKilometer = 2.00;

        String a = input.toJSONString();

        if(a.equals("{\"isEmpty\":true}") || input.get("isMatch").equals(false)){
            return input;
        }
        JSONParser parser = new JSONParser();
        JSONObject obj;
        try {
            obj = (JSONObject) parser.parse(a);
            JSONObject coordinatesA = (JSONObject) obj.get("A");
            Integer aX = Integer.parseInt(coordinatesA.get("x").toString());
            Integer aY = Integer.parseInt(coordinatesA.get("y").toString());

            JSONObject coordinatesB = (JSONObject) obj.get("B");
            Integer bX = Integer.parseInt(coordinatesB.get("x").toString());
            Integer bY = Integer.parseInt(coordinatesB.get("y").toString());


            Integer x = abs(aX - bX);
            Integer y = abs(aY - bY);

            Double totalPrice = (x + y) * pricePerKilometer;

            obj.put("Profit", totalPrice);
            return obj;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
}
