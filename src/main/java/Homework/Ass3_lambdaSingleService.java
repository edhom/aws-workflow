package Homework;

import com.amazonaws.services.lambda.invoke.LambdaFunction;
import java.math.BigInteger;

public interface Ass3_lambdaSingleService {
    @LambdaFunction(functionName="FibonacciS3")
    BigInteger[] calc_fib(int[] input);
}
