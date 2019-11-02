import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.math.BigInteger;

public class Ass3_DistributeFibonacci implements RequestHandler<int[], BigInteger[]> {
    public BigInteger[] handleRequest(int[] input, Context context) {

        AWSLambda awsLambda = AWSLambdaAsyncClientBuilder
                .standard()
                .withRegion(Regions.EU_CENTRAL_1)
                .build();

        final Ass3_lambdaSingleService lambdaService = LambdaInvokerFactory.builder()
                .lambdaClient(awsLambda)
                .build(Ass3_lambdaSingleService.class);

        BigInteger[] result = new BigInteger[input.length];
        int[] inputDivided = new int[1];
        for (int i = 0; i < input.length; i++) {
            inputDivided[0] = input[i];
            result[i] = lambdaService.calc_fib(inputDivided)[0];
        }
        return result;
    }
}
