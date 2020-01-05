package Project;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class f5_OverheadInTime implements RequestHandler<JSONObject, JSONObject> {
    @Override
    public JSONObject handleRequest(JSONObject input, Context context) {
        String a = input.toJSONString();
        JSONParser parserRequest = new JSONParser();

        if(a.equals("{\"isEmpty\":true}")  || input.get("isMatch").equals(false)){
            return input;
        }

        try {
            Double avgSpeed = 50.0;
            JSONObject obj = (JSONObject) parserRequest.parse(a);
            Integer overhead = Integer.parseInt(obj.get("Overhead").toString());
            Double overheadInTime = overhead/avgSpeed;
            obj.put("OverheadInTime", overheadInTime);

            return obj;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
