package Project.Redis;

import org.json.simple.parser.ParseException;

import java.io.IOException;

public class MainEngineRedis {
    @SuppressWarnings("Duplicates")
    public static void main(String[] args) throws ParseException {
        String redisDNS = "";
        try {
            redisDNS = RedisClusterCreator.create();
        } catch(IOException | InterruptedException e) {
            System.err.println("Cluster creation failed");
        }
        EngineWorkflowRedis.initializeStorage(redisDNS);
        long engine1Start = System.currentTimeMillis();
        EngineWorkflowRedis.parseWorkflow("RideOfferAFCL2.yaml");
        long engine1End = System.currentTimeMillis() - engine1Start;

        long engine2Start = System.currentTimeMillis();
        EngineWorkflowRedis.parseWorkflow("RideOfferAFCL2.yaml");
        long engine2End = System.currentTimeMillis() - engine2Start;

        long engine3Start = System.currentTimeMillis();
        EngineWorkflowRedis.parseWorkflow("RideOfferAFCL2.yaml");
        long engine3End = System.currentTimeMillis() - engine3Start;

        long engine4Start = System.currentTimeMillis();
        EngineWorkflowRedis.parseWorkflow("RideOfferAFCL2.yaml");
        long engine4End = System.currentTimeMillis() - engine4Start;

        long engine5Start = System.currentTimeMillis();
        EngineWorkflowRedis.parseWorkflow("RideOfferAFCL2.yaml");
        long engine5End = System.currentTimeMillis() - engine5Start;

        System.out.println("Engine1: " + engine1End);
        System.out.println("Engine2: " + engine2End);
        System.out.println("Engine3: " + engine3End);
        System.out.println("Engine4: " + engine4End);
        System.out.println("Engine5: " + engine5End);
    }
}
