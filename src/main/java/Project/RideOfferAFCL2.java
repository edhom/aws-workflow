package Project;

import com.dps.afcl.Function;
import com.dps.afcl.Workflow;
import com.dps.afcl.functions.*;
import com.dps.afcl.functions.objects.*;
import com.dps.afcl.functions.objects.dataflow.DataFlowBlock;
import com.dps.afcl.functions.objects.dataflow.DataInsDataFlow;
import com.dps.afcl.utils.Utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class RideOfferAFCL2 {
    @SuppressWarnings("Duplicates")
    public static void main(String[] args) {

        // TODO: Pfad setzen
        String path = "C:\\Users\\geige\\Documents\\3_Semester\\02_Verteilte Systeme\\distributed_systems\\src\\main\\resources\\schema.json";

        // Create a new workflow
        Workflow workflow = new Workflow();
        workflow.setName("RideOffer");

        // f1_rideRequest
        AtomicFunction f1_rideRequest = new AtomicFunction("f1_RideRequest", "Function", null, Arrays.asList(new DataOutsAtomic("OutVal1", "Collection"), new DataOutsAtomic("NumRequests", "ArrayLength")));


        // parallelFor RideRequest
        ParallelFor parallelFor = new ParallelFor();
        parallelFor.setName("parallelFor");
        DataInsDataFlow dataIns = new DataInsDataFlow("OutVal1","Collection", f1_rideRequest.getName() + "/" + f1_rideRequest.getDataOuts().get(0).getName());

        dataIns.setDataFlow(new DataFlowBlock("1"));
        parallelFor.setDataIns(Arrays.asList(dataIns));

        parallelFor.setLoopCounter(new LoopCounter("Counter", "Counter", "0", "inputSize", "1")); // getDataOutsByIndex(f1_rideRequest, 1)

        // checkMatch
        AtomicFunction f2_checkMatches = new AtomicFunction("f2_checkMatches", "Function", Arrays.asList(new DataIns("InVal3", "Request", parallelFor.getName() +  "/" + parallelFor.getDataIns().get(0).getName())), Arrays.asList(new DataOutsAtomic("OutVal3", "Request")));


        //informPassenger
        AtomicFunction f3_CalcProfit = new AtomicFunction("f3_CalcProfit", "Function", Arrays.asList(new DataIns("InVal4", "Request", f2_checkMatches.getName() + "/" + f2_checkMatches.getDataOuts().get(0).getName())), Arrays.asList(new DataOutsAtomic("OutVal4", "Request")));
        //calcTimeToGate
        AtomicFunction f4_calculateOverheadKM = new AtomicFunction("f4_calculateOverheadKM", "Function", Arrays.asList(new DataIns("InVal5", "Request", f3_CalcProfit.getName() + "/" + f3_CalcProfit.getDataOuts().get(0).getName())), Arrays.asList(new DataOutsAtomic("OutVal5", "Request")));
        AtomicFunction f5_OverheadInTime = new AtomicFunction("f5_OverheadInTime", "Function", Arrays.asList(new DataIns("InVal6", "Request", f4_calculateOverheadKM.getName() + "/" + f4_calculateOverheadKM.getDataOuts().get(0).getName())), Arrays.asList(new DataOutsAtomic("OutVal6", "Request")));


        parallelFor.setLoopBody(Arrays.asList(f2_checkMatches, f3_CalcProfit, f4_calculateOverheadKM, f5_OverheadInTime)); //, f3_calcProfit, f4_calcOverhead, f5_calcOverheadInTime

        // TODO: Output wieder Collection??
        parallelFor.setDataOuts(Arrays.asList(new DataOuts("OutVal9", "Collection", getDataOutsByIndex(f5_OverheadInTime,0))));

        // f6_optimalRideRequest just one request as output, not collection
        AtomicFunction f6_optimalRideRequest = new AtomicFunction("f6_optimalRideRequest", "Function", Arrays.asList(new DataIns("InVal7", "Collection", parallelFor.getName()+"/"+parallelFor.getDataOuts().get(0).getName())), Arrays.asList(new DataOutsAtomic("OutVal7", "Request")));

        // f7_optimalPickUp
        AtomicFunction f7_optimalPickUp = new AtomicFunction("f7_OptimalPickUp", "Function", Arrays.asList(new DataIns("InVal8", "Request", f6_optimalRideRequest.getName() + "/" + f6_optimalRideRequest.getDataOuts().get(0).getName())), Arrays.asList(new DataOutsAtomic("OutVal8", "Request")));


        // parallel f8 and f9
        Parallel parallelF8F9 = new Parallel();
        parallelF8F9.setName("parallelF8F9");
        DataInsDataFlow dataInsParallel2 = new DataInsDataFlow("InVal9", "Request", f7_optimalPickUp.getName() + "/" + f7_optimalPickUp.getDataOuts().get(0).getName());
        parallelF8F9.setDataIns(Arrays.asList(dataInsParallel2));

        // f8_informPassenger
        AtomicFunction f8_informPassenger = new AtomicFunction("f8_informPassenger", "Function", Arrays.asList(new DataIns("InVal9", "Request",f7_optimalPickUp.getName() + "/" + f7_optimalPickUp.getDataOuts().get(0).getName())), Arrays.asList(new DataOutsAtomic("OutVal9", "Boolean")));

        // f9_informDriver
        AtomicFunction f9_informDriver = new AtomicFunction("f9_InformDriver", "Function",Arrays.asList(new DataIns("InVal9", "Request",f7_optimalPickUp.getName() + "/" + f7_optimalPickUp.getDataOuts().get(0).getName())), Arrays.asList(new DataOutsAtomic("OutVal10", "Boolean")));


        parallelF8F9.setParallelBody(Arrays.asList(new Section(Arrays.asList(f8_informPassenger)), new Section(Arrays.asList(f9_informDriver))));
        parallelF8F9.setDataOuts(Arrays.asList(new DataOuts("OutVal14", "Request", getDataOutsByIndex(f8_informPassenger,0) + ", "+ getDataOutsByIndex(f9_informDriver, 0))));


        AtomicFunction f10_logDatabase = new AtomicFunction("f10_logInDatabase", "f10_logDatabaseType", Arrays.asList(new DataIns("InVal9", "Request",f7_optimalPickUp.getName() + "/" + f7_optimalPickUp.getDataOuts().get(0).getName())), Arrays.asList(new DataOutsAtomic("OutVal11", "String")));



        // Set all compounds as a sequence
        workflow.setWorkflowBody(Arrays.asList(f1_rideRequest, parallelFor, f6_optimalRideRequest, f7_optimalPickUp, parallelF8F9, f10_logDatabase));

        // Validate workflow and write as YAML
        Utils.writeYaml(workflow, "RideOfferAFCL2.yaml", path);


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
