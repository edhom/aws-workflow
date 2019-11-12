import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory;
import org.apache.log4j.BasicConfigurator;
import java.text.DecimalFormat;

public class Ass4_main {
    @SuppressWarnings("Duplicates")
    public static void main(String[] args) {
        BasicConfigurator.configure();

        // Get AWS Client
        BasicAWSCredentials awsCredentials = GeneralUtils.loadCredentialsFromConfig();
        AWSLambda awsLambda = AWSLambdaClientBuilder
                .standard()
                .withRegion(Regions.EU_CENTRAL_1)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();

        final Ass4_lambdaAsyncFibonacciService asyncFibService = LambdaInvokerFactory.builder()
                .lambdaClient(awsLambda)
                .build(Ass4_lambdaAsyncFibonacciService.class);

        // Fill input Array
        Integer[] input = new Integer[35];
        for (int i = 0; i < input.length; i++) {
            input[i] = i;
        }
        periodic(awsLambda, asyncFibService, input);
        //eventDriven(awsLambda, asyncFibService, input);

    }

    public static void eventDriven(AWSLambda awsLambda, Ass4_lambdaAsyncFibonacciService asyncFibService, Integer[] input){
        long runtimeAsync = System.currentTimeMillis();
        asyncFibService.invoke(input);
        double asyncFibonacci = System.currentTimeMillis() - runtimeAsync;
        DecimalFormat f = new DecimalFormat("#0.00000");
        System.out.println("\nAsynchronous Fibonacci calculation took ");
        System.out.println("Execution: "  + f.format(asyncFibonacci/ ((double) 1000)) + " sec");
    }

    public static void periodic(AWSLambda awsLambda, Ass4_lambdaAsyncFibonacciService asyncFibService, Integer[] input){

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                asyncFibService.invoke(input);
            }
        });

        final Ass4_lambdaCheckResultService checkResultFibService = LambdaInvokerFactory.builder()
                .lambdaClient(awsLambda)
                .build(Ass4_lambdaCheckResultService.class);

        final int[] loop = new int[1];
        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {

                while (!checkResultFibService.handleRequest(input.length)) {
                    try {
                        Thread.sleep(1000);
                        loop[0]++;
                    } catch (InterruptedException e) {
                        System.err.println("\nThread.sleep() was interrupted!");
                        System.exit(1);
                    }
                }
            }
        });


        long runtimeAsync = System.currentTimeMillis();
        try {
            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        double asyncFibonacci = System.currentTimeMillis() - runtimeAsync;

        System.out.println("\n\nInvoked CheckResult " + loop[0] + " times");


        DecimalFormat f = new DecimalFormat("#0.00000");
        System.out.println("\nAsynchronous Fibonacci calculation took ");
        System.out.println("Execution: "  + f.format(asyncFibonacci/ ((double) 1000)) + " sec");
    }
}
