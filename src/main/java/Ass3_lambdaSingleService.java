import com.amazonaws.services.lambda.invoke.LambdaFunction;
import java.math.BigInteger;

public interface Ass3_lambdaSingleService {
    @LambdaFunction(functionName="Fibonacci")
    BigInteger[] calc_fib(int[] input);
}
