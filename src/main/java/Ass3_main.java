import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.fasterxml.jackson.databind.ObjectMapper;


public class Ass3_main {
    public static void main(String[] args) {
        InvokeRequest invokeRequest = new InvokeRequest()
                .withFunctionName("Fibonacci")
                .withPayload("[0,1,2,3,4,5,6,7,8,9]");

        BasicAWSCredentials awsCredentials = GeneralUtils.loadCredentialsFromConfig();


        AWSLambdaClientBuilder builder = AWSLambdaClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.EU_CENTRAL_1);
        //AWSLambda builder = AWSLambdaClientBuilder.standard()
               // .withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build();
                //.withRegion(Regions.EU_CENTRAL_1);


        AWSLambda awsLambda = builder.build();

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


