package Homework;

import com.amazonaws.services.lambda.invoke.LambdaFunction;

import java.math.BigInteger;

public interface Ass4_lambdaAsyncFibonacciService {
    @LambdaFunction(functionName="AsynchFibonacci")
    Boolean invoke(Integer[] input);
}
