import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * args[...]... InstanceType(s) (String) i.e. "t2.large t2.micro"
 */

public class Ass1_Task_2_Main {
    public static void main(final String[] args)throws Exception {

        ExecutorService executor = Executors.newCachedThreadPool();

        int childID = 0;
        ArrayList<Callable> tasks = new ArrayList<Callable>();
        for(final String arg : args)
        {
            final int finalChildID = childID;
            Callable<Void> task = new Callable<Void>()
            {
                @Override
                public Void call() throws Exception
                {
                    String taskArgs[] = {arg, String.valueOf(finalChildID)};
                    Ass1_Task_2_Child.main(taskArgs);
                    return null;
                }
            };

            executor.submit(task);
            childID++;
        }

        //cleanup executor
       executor.shutdown();

        //Waiting for child processes to terminate
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.out.println("IntterruptedExecption");
        }

    }

}
