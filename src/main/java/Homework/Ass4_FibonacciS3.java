package Homework;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.math.BigInteger;

public class Ass4_FibonacciS3 implements RequestHandler<int[], BigInteger[]> {
    private String bucketName = "dhom.distributedsystems.fib";

    AmazonS3 s3client = AmazonS3ClientBuilder
            .standard()
            .withRegion(Regions.EU_CENTRAL_1)
            .build();

    public BigInteger[] handleRequest(int[] input, Context context) {
        BigInteger[] result = new BigInteger[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = fib(input[i]);
        }
        storeInS3(result, input[0]);
        return result;
    }

    public BigInteger fib(int n) {
        if (n == 0){
            return BigInteger.ZERO;
        } else if (n == 1){
            return BigInteger.ONE;
        }
        return fib(n - 2).add(fib(n - 1));
    }

    public void storeInS3(BigInteger[] result, Integer func_num) {
        s3client.putObject(bucketName, "result" + func_num.toString(), GeneralUtils.ArrayToString(result));
    }


}
