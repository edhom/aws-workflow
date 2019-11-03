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
        for (int i = 0; i < input.length; i++) {
            input[i] = i;
        }


        // Invoke SingleFibonacci and DistributeFibonacci
        long[] singleFibonacci = new long[5];
        BigInteger[] result_single = new BigInteger[35];
        BigInteger[] result_distributed = new BigInteger[35];
        long[] distributeFibonacci = new long[5];
        for(int i = 0; i < 5; i++){
            long runtimeSingle = System.currentTimeMillis();
            result_single = singleFibService.calc_fib(input);
            runtimeSingle = System.currentTimeMillis() - runtimeSingle;
            singleFibonacci[0] = runtimeSingle;

            long runtimeDistribute = System.currentTimeMillis();
            result_distributed = distributeFibService.calc_fib(input);
            runtimeDistribute = System.currentTimeMillis() - runtimeDistribute;
            distributeFibonacci[0] = runtimeDistribute;
        }

        System.out.println("\nSingle Fibonacci calculation took ");
        System.out.println(GeneralUtils.ArrayToString(result_single));
        for(Long single : singleFibonacci){
            System.out.println("1. Execution: "  + single/ ((double) 1000) + " sec");
        }

        System.out.println("\nDistributed Fibonacci calculation took ");
        System.out.println(GeneralUtils.ArrayToString(result_distributed));
        for(Long distributed : distributeFibonacci){
            System.out.println("1. Execution: "  + distributed/ ((double) 1000) + " sec");
        }


    }
}

