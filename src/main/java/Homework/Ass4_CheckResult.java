package Homework;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import java.util.StringJoiner;

public class Ass4_CheckResult implements RequestHandler<Integer, Boolean> {
    private String bucketName = "dhom.distributedsystems.fib";

    AmazonS3 s3client = AmazonS3ClientBuilder
            .standard()
            .withRegion(Regions.EU_CENTRAL_1)
            .build();

    public Boolean handleRequest(Integer num, Context b) {

        StringJoiner res = new StringJoiner(", ");

        for (int i = 0; i < num; i++) {
            String fileName = "result" + i;
            if (!s3client.doesObjectExist(bucketName, fileName)) {
                return false;
            }
            S3Object object = s3client.getObject(new GetObjectRequest(bucketName, fileName));
            String content = GeneralUtils.getStringFromInputStream(object.getObjectContent());
            res.add(content);
        }
        String resultFormatted = '[' + res.toString() + ']';
        s3client.putObject(bucketName, "ResultCollection", resultFormatted);
        return true;
    }


}
