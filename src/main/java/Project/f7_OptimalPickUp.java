package Project;

import Homework.GeneralUtils;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class f7_OptimalPickUp implements RequestHandler<JSONObject, JSONObject> {
    String bucketName = "ride.offer.geiger";

    AmazonS3 s3client = AmazonS3ClientBuilder
            .standard()
            .withRegion(Regions.EU_CENTRAL_1)
            .build();

    @SuppressWarnings("Duplicates")
    @Override
    public JSONObject handleRequest(JSONObject input, Context context) {

        if(input == null){
            return input;
        }

        String a = input.toJSONString();
        JSONParser parserRequest = new JSONParser();

        if(a.equals("{\"isEmpty\":true}")){
            return input;
        }

        S3Object object = s3client.getObject(new GetObjectRequest(bucketName, "driver.json"));
        String content = GeneralUtils.getStringFromInputStream(object.getObjectContent());
        JSONParser parserDriver = new JSONParser();
        try {
            JSONObject objRequest = (JSONObject) parserRequest.parse(a);
            JSONObject coordinatesARequest = (JSONObject) objRequest.get("A");
            Integer aXRequest = Integer.parseInt(coordinatesARequest.get("x").toString());
            Integer aYRequest = Integer.parseInt(coordinatesARequest.get("y").toString());

            JSONObject objDriver = (JSONObject) parserDriver.parse(content);
            JSONObject coordinatesADriver = (JSONObject) objDriver.get("A");
            Integer aXDriver = Integer.parseInt(coordinatesADriver.get("x").toString());
            Integer aYDriver = Integer.parseInt(coordinatesADriver.get("y").toString());

            Integer x = Math.abs(aXRequest - aXDriver);
            Integer y = Math.abs(aYRequest - aYDriver);

            Integer minutes = (x + y);

            JSONObject pickUp = (JSONObject) objRequest.get("OptimalPickUp");
            pickUp.put("x", aXRequest);
            pickUp.put("y", aYRequest);
            pickUp.put("inMinutes", minutes);

            return objRequest;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
}
