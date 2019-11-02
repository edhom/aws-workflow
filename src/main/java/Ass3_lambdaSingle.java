import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.math.BigInteger;

public class Ass3_lambdaSingle implements RequestHandler<int[], BigInteger[]> {
    public BigInteger[] handleRequest(int[] input, Context context) {
        BigInteger[] result = new BigInteger[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = fib(input[i]);
            LambdaLogger logger = context.getLogger();
            logger.log("calculated: " + result[i]);
        }
        return result;
    }

    public BigInteger fib(int n) {
        if (n == 0){
            return BigInteger.ZERO;
        } else if (n == 1){
            return BigInteger.ONE;
        }
        return fib(n - 2).add(fib(n - 1));
    }

}
