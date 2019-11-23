package Project;

import Homework.GeneralUtils;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.StringJoiner;

public class f1_RideRequest implements RequestHandler<Void, String> {
    private String bucketName = "ride.offer.geiger";

    AmazonS3 s3client = AmazonS3ClientBuilder
            .standard()
            .withRegion(Regions.EU_CENTRAL_1)
            .build();

    @Override
    public String handleRequest(Void input, Context context) {
        StringJoiner res = new StringJoiner(", ");

        S3Objects.inBucket(s3client, bucketName).forEach((S3ObjectSummary objectSummary) -> {
            S3Object object = s3client.getObject(new GetObjectRequest(bucketName, objectSummary.getKey()));
            String content = GeneralUtils.getStringFromInputStream(object.getObjectContent());
            res.add(content);
        });

        return '[' + res.toString() + ']';
    }
}
