import com.amazonaws.services.lambda.invoke.LambdaFunction;

public interface Ass4_lambdaCheckResultService {
    @LambdaFunction(functionName="CheckResult")
    Boolean handleRequest(Integer num);
}
