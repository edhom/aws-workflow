package Project;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class f9_InformDriver implements RequestHandler<JSONObject, String> {
    private String bucketName = "ride.offer.geiger";

    AmazonS3 s3client = AmazonS3ClientBuilder
            .standard()
            .withRegion("eu-central-1")
            .build();


    @Override
    public String handleRequest(JSONObject input, Context context) {

        String message = "";

        if(input == null){
            message = "Can not find a proper request";
        }

        else{
            String a = input.toJSONString();
            JSONParser parser = new JSONParser();


            if(a.equals("[{\"isEmpty\":true}]")){
                message = "Can not find any requests";
            }

            try {
                JSONObject obj = (JSONObject) parser.parse(a);
                JSONObject optimalPickUp = (JSONObject) obj.get("OptimalPickUp");

                message = "Pickup in " + optimalPickUp.get("inMinutes") + " minutes at Location: (" + optimalPickUp.get("x") + "," + optimalPickUp.get("y") + ")";

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


        s3client.putObject(bucketName, "InformationDriver.txt", message);
        return "Driver informed!";
    }
}
