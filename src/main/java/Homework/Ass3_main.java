package Homework;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory;
import org.apache.log4j.BasicConfigurator;

import java.math.BigInteger;
import java.text.DecimalFormat;


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
        double[] singleFibonacci = new double[5];
        BigInteger[] result_single = new BigInteger[35];
        BigInteger[] result_distributed = new BigInteger[35];
        double[] distributeFibonacci = new double[5];
        DecimalFormat f = new DecimalFormat("#0.00000");
        for(int i = 0; i < 5; i++){
            long runtimeSingle = System.currentTimeMillis();
            result_single = singleFibService.calc_fib(input);
            runtimeSingle = System.currentTimeMillis() - runtimeSingle;
            singleFibonacci[i] = runtimeSingle;

            long runtimeDistribute = System.currentTimeMillis();
            result_distributed = distributeFibService.calc_fib(input);
            runtimeDistribute = System.currentTimeMillis() - runtimeDistribute;
            distributeFibonacci[i] = runtimeDistribute;
        }

        System.out.println("\nSingle Fibonacci calculation took ");
        System.out.println(GeneralUtils.ArrayToString(result_single));
        for(int i = 0; i < 5; i++){
            System.out.println(i+1 + ". Execution: "  + f.format(singleFibonacci[i]/ ((double) 1000)) + " sec");
        }

        System.out.println("\nDistributed Fibonacci calculation took ");
        System.out.println(GeneralUtils.ArrayToString(result_distributed));
        for(int i = 0; i < 5; i++){
            System.out.println(i+1 + ".Execution: "  + f.format(distributeFibonacci[i]/ ((double) 1000)) + " sec");
        }


    }
}

