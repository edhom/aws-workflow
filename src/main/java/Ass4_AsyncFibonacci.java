import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.concurrent.Future;

public class Ass4_AsyncFibonacci implements RequestHandler<Integer[], Boolean> {
    public Boolean handleRequest(Integer[] input, Context context) {

        AWSLambdaAsync awsLambda = AWSLambdaAsyncClientBuilder
                .standard()
                .withRegion(Regions.EU_CENTRAL_1)
                .build();

        Future[] arr = new Future[input.length];

        for (int i = 0; i < input.length; i++) {
            InvokeRequest req = new InvokeRequest()
                    .withFunctionName("FibonacciS3")
                    .withPayload(input[i].toString());

            arr[i] = awsLambda.invokeAsync(req);
        }
        for (int i = 0; i < input.length; i++) {
            while (!arr[i].isDone()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.err.println("\nThread.sleep() was interrupted!");
                    System.exit(1);
                }
            }
        }

        InvokeRequest collectRequest = new InvokeRequest()
                .withFunctionName("CheckResult")
                .withPayload(String.valueOf(input.length));
        InvokeResult result = awsLambda.invoke(collectRequest);

        if(result.getStatusCode() > 0){
            return true;
        }
        return false;
    }
}


