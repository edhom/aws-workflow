package Project.Redis;

import org.json.simple.parser.ParseException;

public class MainEngineRedis {
    public static void main(String[] args) throws ParseException {
        EngineWorkflowRedis.parseWorkflow("RideOfferAFCL2.yaml");
    }
}
