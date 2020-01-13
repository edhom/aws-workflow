package Project.Redis;

import org.json.simple.parser.ParseException;

import java.io.IOException;

public class MainEngineRedis {
    public static void main(String[] args) throws ParseException {
        String redisDNS = "";
        try {
            redisDNS = RedisClusterCreator.create();
        } catch(IOException | InterruptedException e) {
            System.err.println("Cluster creation failed");
        }
        EngineWorkflowRedis.initializeStorage(redisDNS);
        EngineWorkflowRedis.parseWorkflow("RideOfferAFCL2.yaml");
    }
}
