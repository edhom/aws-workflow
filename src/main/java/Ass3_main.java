import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory;
import org.apache.log4j.BasicConfigurator;

import java.math.BigInteger;
/*
public class Ass3_main {
    public static void main(String[] args) {
        InvokeRequest invokeRequest = new InvokeRequest()
                .withFunctionName("Fibonacchi")
                .withPayload("[0,1,2,3,4,5,6,7,8,9]");

        BasicAWSCredentials awsCredentials = GeneralUtils.loadCredentialsFromConfig();

        AWSLambda awsLambda = AWSLambdaClientBuilder.standard()
                .withRegion(Regions.EU_CENTRAL_1)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build();

        InvokeResult invokeResult = null;

        try {
            invokeResult = awsLambda.invoke(invokeRequest);
        }
        catch (Exception e) {
            System.out.println(e);
        }

        System.out.println(invokeResult.getStatusCode());
    }
}
*/

public class Ass3_main {
    public static void main(String[] args) {
        BasicConfigurator.configure();
        BasicAWSCredentials awsCredentials = GeneralUtils.loadCredentialsFromConfig();
        AWSLambda awsLambda = AWSLambdaClientBuilder
                .standard()
                .withRegion(Regions.EU_CENTRAL_1)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();

        final Ass3_lambdaSingleService lambdaService = LambdaInvokerFactory.builder()
                .lambdaClient(awsLambda)
                .build(Ass3_lambdaSingleService.class);

        int[] input = {0,1,2,3,4,5,6,7,8,9};
        BigInteger[] result = lambdaService.calc_fib(input);
    }
}

