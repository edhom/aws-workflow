package Project;

import Homework.GeneralUtils;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.List;

public class f1_RideRequest implements RequestHandler<Void, JSONArray> {
    @SuppressWarnings("Duplicates")
    private String bucketName = "dhom-distributedsystems-rideoffer";

    AmazonS3 s3client = AmazonS3ClientBuilder
            .standard()
            .withRegion(Regions.EU_CENTRAL_1)
            .build();

    @Override
    public JSONArray handleRequest(Void input, Context context) {
        JSONArray jsonArray = new JSONArray();
        ObjectListing listing = s3client.listObjects(bucketName, "input");
        List<S3ObjectSummary> summaries = listing.getObjectSummaries();

        while (listing.isTruncated()) {
            listing = s3client.listNextBatchOfObjects (listing);
            summaries.addAll (listing.getObjectSummaries());
        }

        for(S3ObjectSummary file : summaries){
            S3Object object = s3client.getObject(new GetObjectRequest(bucketName, file.getKey()));
            String content = GeneralUtils.getStringFromInputStream(object.getObjectContent());
            JSONParser parser = new JSONParser();
            try {
                JSONObject obj = (JSONObject) parser.parse(content);
                jsonArray.add(obj);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return jsonArray.size() == 0 ? null : jsonArray;
    }
}
