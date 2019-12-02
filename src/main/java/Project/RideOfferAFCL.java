package Project;

import com.dps.afcl.Function;
import com.dps.afcl.Workflow;
import com.dps.afcl.functions.*;
import com.dps.afcl.functions.objects.*;
import com.dps.afcl.functions.objects.dataflow.DataFlowBlock;
import com.dps.afcl.functions.objects.dataflow.DataInsDataFlow;
import com.dps.afcl.utils.Utils;
import java.util.Arrays;

public class RideOfferAFCL {
    @SuppressWarnings("Duplicates")
    public static void main(String[] args) {

        // TODO: Pfad setzen
        String path = "/Users/ericdhom/Desktop/Uni/Semester3/VerteilteSysteme/Repo/src/main/resources/schema.json";

        // Create a new workflow
        Workflow workflow = new Workflow();
        workflow.setName("RideOffer");

        // f1_rideRequest
        AtomicFunction f1_rideRequest = new AtomicFunction("f1_rideRequest", "f1_rideRequestType", null, Arrays.asList(new DataOutsAtomic("OutVal1", "collection"), new DataOutsAtomic("numRequests", "number")));

        // ifThenElse for ride request list
        IfThenElse ifThenElse1 = new IfThenElse();
        ifThenElse1.setName("ifThenElse1");

        // TODO: Contition richtig?
        ifThenElse1.setDataIns(Arrays.asList(getDataOuts("InVal1", "collection", f1_rideRequest, 0)));
        ifThenElse1.setCondition(new Condition("and", Arrays.asList(new ACondition(getDataInsByIndex(ifThenElse1,0),null,"notEmpty"))));


        // parallelFor RideRequest
        ParallelFor parallelFor = new ParallelFor();
        parallelFor.setName("parallelForRideRequest");
        DataInsDataFlow dataIns = new DataInsDataFlow("InVal2","collection", f1_rideRequest.getName() + "/" + f1_rideRequest.getDataOuts().get(0).getName());

        // TODO: Für was DataFlowBlock? Passt der Pfad für dataIns?
        dataIns.setDataFlow(new DataFlowBlock("1"));
        parallelFor.setDataIns(Arrays.asList(dataIns));

        parallelFor.setLoopCounter(new LoopCounter("counter", "number", "0", getDataOutsByIndex(f1_rideRequest, 1)));

        // checkMatch
        AtomicFunction f2_checkMatch = new AtomicFunction("f2_checkMatch", "f2_checkMatchType", Arrays.asList(new DataIns("InVal3", "request", parallelFor.getName() +  "/" + parallelFor.getDataIns().get(0).getName())), Arrays.asList(new DataOutsAtomic("OutVal3", "boolean")));

        // ifThenElse for checking match
        IfThenElse ifThenElse2 = new IfThenElse();
        ifThenElse2.setName("ifThenElse");
        ifThenElse2.setDataIns(Arrays.asList(getDataOuts("InVal4", "request", f2_checkMatch, 0)));
        ifThenElse2.setCondition(new Condition("and", Arrays.asList(new ACondition(getDataInsByIndex(ifThenElse2,0),"true","=="))));

        // parallel for calcProfit and calcOverhead
        Parallel parallelF3F4 = new Parallel();
        parallelF3F4.setName("parallelF3F4");
        DataInsDataFlow dataInsParallel = new DataInsDataFlow("InVal5", "request", parallelFor.getName() + "/" + parallelFor.getDataIns().get(0).getName());
        parallelF3F4.setDataIns(Arrays.asList(dataInsParallel));

        //informPassenger
        AtomicFunction f3_calcProfit = new AtomicFunction("f3_calcProfit", "f3_calcProfitType", Arrays.asList(getDataIns("InVal6", "request", parallelF3F4, 0)), Arrays.asList(new DataOutsAtomic("OutVal4", "number")));

        //calcTimeToGate
        AtomicFunction f4_calcOverhead = new AtomicFunction("f4_calcOverhead", "f4_calcOverheadType", Arrays.asList(getDataIns("InVal7", "request", parallelF3F4, 0)), Arrays.asList(new DataOutsAtomic("OutVal5", "request")));
        AtomicFunction f5_calcOverheadInTime = new AtomicFunction("f5_calcOverheadInTime", "f5_calcOverheadInTimeType", Arrays.asList(getDataIns("InVal8", "request", parallelF3F4, 0)), Arrays.asList(new DataOutsAtomic("OutVal6", "request")));


        parallelF3F4.setParallelBody(Arrays.asList(new Section(Arrays.asList(f3_calcProfit)), new Section(Arrays.asList(f4_calcOverhead, f5_calcOverheadInTime))));

        parallelF3F4.setDataOuts(Arrays.asList(new DataOuts("OutVal7", "request", getDataOutsByIndex(f3_calcProfit,0) + ", "+ getDataOutsByIndex(f5_calcOverheadInTime, 0))));

        ifThenElse2.setThen(Arrays.asList(parallelF3F4));
        ifThenElse2.setElse(null);
        ifThenElse2.setDataOuts(Arrays.asList(new DataOuts("OutVal8", "request", getDataOutsByIndex(parallelF3F4,0))));


        parallelFor.setLoopBody(Arrays.asList(f2_checkMatch, parallelF3F4));

        // TODO: Output wieder Collection??
        parallelFor.setDataOuts(Arrays.asList(new DataOuts("OutVal9", "request", getDataOutsByIndex(parallelF3F4,0))));


        // f6_optimalRideRequest just one request as output, not collection
        AtomicFunction f6_optimalRideRequest = new AtomicFunction("f6_optimalRideRequest", "f6_optimalRideRequestType", Arrays.asList(new DataIns("InVal9", "collection", parallelFor.getName() + "/" + parallelFor.getDataIns().get(0).getName())), Arrays.asList(new DataOutsAtomic("OutVal10", "request")));

        // f7_optimalPickUp
        AtomicFunction f7_optimalPickUp = new AtomicFunction("f7_optimalPickUp", "f7_optimalPickUpType", Arrays.asList(new DataIns("InVal10", "request", f6_optimalRideRequest.getName() + "/" + f6_optimalRideRequest.getDataIns().get(0).getName())), Arrays.asList(new DataOutsAtomic("OutVal11", "request")));


        // parallel f8 and f9
        Parallel parallelF8F9 = new Parallel();
        parallelF8F9.setName("parallelF3F4");
        DataInsDataFlow dataInsParallel2 = new DataInsDataFlow("InVal", "collection", parallelFor.getName() + "/" + parallelFor.getDataIns().get(0).getName());
        parallelF8F9.setDataIns(Arrays.asList(dataInsParallel2));

        // f8_informPassenger
        AtomicFunction f8_informPassanger = new AtomicFunction("f8_informPassenger", "f8_informPassengerType", Arrays.asList(getDataIns("InVal11", "request", parallelF3F4, 0)), Arrays.asList(new DataOutsAtomic("OutVal12", "boolean")));

        // f9_informDriver
        AtomicFunction f9_informDriver = new AtomicFunction("f9_informDriver", "f9_informDriverType", Arrays.asList(getDataIns("InVal12", "request", parallelF3F4, 0)), Arrays.asList(new DataOutsAtomic("OutVal13", "boolean")));


        parallelF8F9.setParallelBody(Arrays.asList(new Section(Arrays.asList(f8_informPassanger)), new Section(Arrays.asList(f9_informDriver))));
        parallelF8F9.setDataOuts(Arrays.asList(new DataOuts("OutVal14", "request", getDataOutsByIndex(f8_informPassanger,0) + ", "+ getDataOutsByIndex(f9_informDriver, 0))));

        AtomicFunction f10_logDatabase = new AtomicFunction("f10_logDatabase", "f10_logDatabaseType", Arrays.asList(getDataOuts("InVal13", "collection", f1_rideRequest, 0)), null);


        ifThenElse1.setThen(Arrays.asList(parallelFor, f6_optimalRideRequest, f7_optimalPickUp, parallelF8F9));
        ifThenElse1.setElse(Arrays.asList(f10_logDatabase));
        ifThenElse1.setDataOuts(Arrays.asList(new DataOuts("OutVal15", "string", getDataOutsByIndex(parallelFor,0) ))); //+ ","+/getDataOutsByIndex(f10_logDatabase,0)


        // Set all compounds as a sequence
        workflow.setWorkflowBody(Arrays.asList(f1_rideRequest, parallelFor, f6_optimalRideRequest, f7_optimalPickUp, parallelF8F9, f10_logDatabase));

        // Validate workflow and write as YAML
        Utils.writeYaml(workflow, "RideOfferAFCL.yaml", path);

        //Utils.readYAML("gateChangeAlert.yaml", )
    }


    /*
     * Helper functions to simplify code
     */
    private static DataIns getDataIns(String name, String type, Function function, int inParamIndex){
        DataIns dataIns = new DataIns();
        if(function instanceof AtomicFunction){
            AtomicFunction atomicFunction = ((AtomicFunction) function);
            dataIns.setSource(atomicFunction.getName() + "/" + atomicFunction.getDataIns().get(inParamIndex).getName());
        } else if(function instanceof CompoundSequential){
            CompoundSequential compoundSequential = (CompoundSequential) function;
            dataIns.setSource(compoundSequential.getName() + "/" + compoundSequential.getDataIns().get(inParamIndex).getName());
        } else if(function instanceof CompoundParallel){
            CompoundParallel compoundParallel = (CompoundParallel) function;
            dataIns.setSource(compoundParallel.getName() + "/" + compoundParallel.getDataIns().get(inParamIndex).getName());
        } else {
            System.err.println("Not supported");
        }
        dataIns.setName(name);
        dataIns.setType(type);
        return dataIns;
    }

    private static String getDataInsByIndex(Function function, int index){
        if(function instanceof AtomicFunction){
            AtomicFunction atomicFunction = (AtomicFunction) function;
            return atomicFunction.getName() + "/" + atomicFunction.getDataIns().get(index).getName();
        }else if (function instanceof CompoundSequential){
            CompoundSequential compoundSequential = (CompoundSequential) function;
            return compoundSequential.getName() + "/" + compoundSequential.getDataIns().get(index).getName();
        }else if (function instanceof CompoundParallel){
            CompoundParallel compoundParallel = (CompoundParallel) function;
            return compoundParallel.getName() + "/" + compoundParallel.getDataIns().get(index).getName();
        }
        System.err.println("Not supported");
        return null;
    }

    private static DataIns getDataOuts(String name, String type, Function function, int outParamIndex){
        DataIns dataIns = new DataIns();
        if(function instanceof AtomicFunction){
            AtomicFunction atomicFunction = ((AtomicFunction) function);
            dataIns.setSource(atomicFunction.getName() + "/" + atomicFunction.getDataOuts().get(outParamIndex).getName());
        } else if(function instanceof Switch){
            Switch _switch = (Switch) function;
            dataIns.setSource(_switch.getName() + "/" + _switch.getDataOuts().get(outParamIndex).getName());
        } else if(function instanceof Parallel){
            Parallel parallel = (Parallel) function;
            dataIns.setSource(parallel.getName() + "/" + parallel.getDataOuts().get(outParamIndex).getName());
        }else if(function instanceof ParallelFor){
            ParallelFor parallelFor = (ParallelFor) function;
            dataIns.setSource(parallelFor.getName() + "/" + parallelFor.getDataOuts().get(outParamIndex).getName());
        }
        else {
            System.err.println("Not supported");
        }
        dataIns.setName(name);
        dataIns.setType(type);
        return dataIns;
    }

    private static String getDataOutsByIndex(Function function, int index){
        if(function instanceof AtomicFunction){
            AtomicFunction atomicFunction = (AtomicFunction) function;
            return atomicFunction.getName() + "/" + atomicFunction.getDataOuts().get(index).getName();
        }else if (function instanceof CompoundSequential){
            CompoundSequential compoundSequential = (CompoundSequential) function;
            return compoundSequential.getName() + "/" + compoundSequential.getDataOuts().get(index).getName();
        }else if (function instanceof CompoundParallel){
            CompoundParallel compoundParallel = (CompoundParallel) function;
            return compoundParallel.getName() + "/" + compoundParallel.getDataOuts().get(index).getName();
        }
        System.err.println("Not supported");
        return null;
    }
}
