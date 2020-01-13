package Project.second;

import Project.Engine.EngineWorkflow;
import org.json.simple.parser.ParseException;

public class MainEngine {
    public static void main(String[] args) throws ParseException {
        EngineWorkflow.parseWorkflow("RideOfferAFCL2.yaml");
    }
}
