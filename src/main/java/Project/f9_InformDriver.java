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
import javax.activation.*;

public class f9_InformDriver implements RequestHandler<JSONObject, String> {
    private String bucketName = "ride.offer.geiger";

    AmazonS3 s3client = AmazonS3ClientBuilder
            .standard()
            .withRegion(Regions.EU_CENTRAL_1)
            .build();


    @Override
    public String handleRequest(JSONObject input, Context context) {

        String a = input.toJSONString();
        JSONParser parser = new JSONParser();
        try {
            JSONObject obj = (JSONObject) parser.parse(a);
            JSONObject optimalPickUp = (JSONObject) obj.get("OptimalPickUp");

            String message = "Time (in minutes): " + optimalPickUp.get("inMinutes") + "Location coordinates: "
                    + "(" + optimalPickUp.get("x") + "," + optimalPickUp.get("y") + ")";

            s3client.putObject(bucketName, "InformationDriver.txt", message);
            sendEmail(message);
            return message;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendEmail(String text){
        // Recipient's email ID needs to be mentioned.
        String to = "laura.geiger@student.uibk.ac.at";

        // Sender's email ID needs to be mentioned
        String from = "lauraxgeiger@icloud.com";

        // Assuming you are sending email from localhost
        String host = "localhost";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty("mail.smtp.host", host);

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject("Ride App new Passenger");

            // Now set the actual message
            message.setText(text);

            // Send message
            Transport.send(message);
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}
