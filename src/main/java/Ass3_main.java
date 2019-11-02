import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory;
import org.apache.log4j.BasicConfigurator;

import java.math.BigInteger;


public class Ass3_main {
    public static void main(String[] args) {
        BasicConfigurator.configure();
        // Get AWS Client
        BasicAWSCredentials awsCredentials = GeneralUtils.loadCredentialsFromConfig();
        AWSLambda awsLambda = AWSLambdaClientBuilder
                .standard()
                .withRegion(Regions.EU_CENTRAL_1)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();

        // Get Lambda Function SingleFibonacci
        final Ass3_lambdaSingleService singleFibService = LambdaInvokerFactory.builder()
                .lambdaClient(awsLambda)
                .build(Ass3_lambdaSingleService.class);
        // Get Lambda Function DistributeFibonacci
        final Ass3_lambdaDistributedService distributeFibService = LambdaInvokerFactory.builder()
                .lambdaClient(awsLambda)
                .build(Ass3_lambdaDistributedService.class);

        // Fill input Array
        int[] input = new int[36];
        for (int i = 0; i < 36; i++) {
            input[i] = i;
        }

        // Invoke SingleFibonacci
        long runtimeSingle = System.currentTimeMillis();
        BigInteger[] result_single = singleFibService.calc_fib(input);
        runtimeSingle = System.currentTimeMillis() - runtimeSingle;
        // Invoke DistributeFibonacci
        long runtimeDistribute = System.currentTimeMillis();
        BigInteger[] result_distributed = distributeFibService.calc_fib(input);
        runtimeDistribute = System.currentTimeMillis() - runtimeDistribute;


        System.out.println("Single Fibonacci Calculation took " + runtimeSingle/ ((float) 1000) + " sec");
        System.out.println("Result = " + GeneralUtils.ArrayToString(result_single));
        System.out.println("Distribute Fibonacci Calculation took " + runtimeDistribute/ ((float) 1000) + " sec");
        System.out.println("Result = " + GeneralUtils.ArrayToString(result_distributed));


    }
}

