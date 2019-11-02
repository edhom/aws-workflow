import com.amazonaws.services.lambda.invoke.LambdaFunction;
import java.math.BigInteger;

public interface Ass3_lambdaDistributedService {
    @LambdaFunction(functionName="DistributeFibonacci")
    BigInteger[] calc_fib(int[] input);
}
