import com.amazonaws.services.lambda.invoke.LambdaFunction;
import java.math.BigInteger;

public interface Ass3_lambdaSingleService {
    @LambdaFunction(functionName="Fibonacchi")
    BigInteger[] calc_fib(String input);
}
