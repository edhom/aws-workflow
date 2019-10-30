import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.math.BigInteger;

public class Assignment3 implements RequestHandler<Integer, String> {
    public String handleRequest(Integer myCount, Context context) {
        String result = fib(myCount).toString();
        LambdaLogger logger = context.getLogger();
        logger.log("calculated : " + result);
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
