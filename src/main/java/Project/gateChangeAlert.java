package Homework05;

import com.dps.afcl.Function;
import com.dps.afcl.Workflow;
import com.dps.afcl.functions.*;
import com.dps.afcl.functions.objects.*;
import com.dps.afcl.functions.objects.dataflow.DataFlowBlock;
import com.dps.afcl.functions.objects.dataflow.DataInsDataFlow;
import com.dps.afcl.utils.Utils;

import java.util.Arrays;

public class gateChangeAlert {

    public static void main(String[] args) {

        // Create a new workflow
        Workflow workflow = new Workflow();
        workflow.setName("gateChangeAlert");

        // Set example workflow input
        workflow.setDataIns(Arrays.asList(new DataIns("InVal", "number", "some source")));

        // getFlight
        AtomicFunction getFlight = new AtomicFunction("getFlight", "getFlightType", Arrays.asList(new DataIns("InVal", "number", workflow.getName() + "/" + workflow.getDataIns().get(0).getName())), Arrays.asList(new DataOutsAtomic("OutVal", "string")));

        // selectPassenger
        AtomicFunction selectPassenger = new AtomicFunction("selectPassenger", "selectPassengerType", Arrays.asList(getDataOuts("InVal", "string", getFlight, 0)), Arrays.asList(new DataOutsAtomic("OutVal", "collection"), new DataOutsAtomic("OutVal2", "number")));

        // parallelFor
        ParallelFor parallelFor = new ParallelFor();
        parallelFor.setName("parallelFor");
        DataInsDataFlow dataIns = new DataInsDataFlow("InVal","collection", selectPassenger.getName() + "/" + selectPassenger.getDataOuts().get(0).getName());
        dataIns.setDataFlow(new DataFlowBlock("5"));
        parallelFor.setDataIns(Arrays.asList(dataIns));
        parallelFor.setLoopCounter(new LoopCounter("counter", "number", "0", getDataOutsByIndex(selectPassenger,1)));

        // parallel (informPassenger and calculateTimeToGate)
        Parallel parallelF3F4 = new Parallel();
        parallelF3F4.setName("parallelF3F4");
        DataInsDataFlow dataInsParallel = new DataInsDataFlow("InVal", "collection", parallelFor.getName() + "/" + parallelFor.getDataIns().get(0).getName());
        parallelF3F4.setDataIns(Arrays.asList(dataInsParallel));
        AtomicFunction informPassenger = new AtomicFunction("informPassenger", "informPassengerType", Arrays.asList(getDataIns("InVal", "string", parallelF3F4, 0)), null);
        AtomicFunction calculateTimeToGate = new AtomicFunction("calculateTimeToGate", "calculateTimeToGateType", Arrays.asList(getDataIns("InVal", "string", parallelF3F4, 0)), Arrays.asList(new DataOutsAtomic("OutVal", "number")));

        // ifThenElse (recommendShop and informTimeCritical)
        IfThenElse ifThenElse = new IfThenElse();
        ifThenElse.setName("ifThenElse");
        ifThenElse.setDataIns(Arrays.asList(getDataOuts("InVal", "number", calculateTimeToGate, 0)));
        ifThenElse.setCondition(new Condition("and", Arrays.asList(new ACondition(getDataInsByIndex(ifThenElse,0),"20",">"))));
        AtomicFunction recommendShop = new AtomicFunction("recommendShop", "recommendShopType", Arrays.asList(getDataIns("InVal", "number", ifThenElse, 0)), Arrays.asList(new DataOutsAtomic("OutVal", "string")));
        AtomicFunction informTimeCritical = new AtomicFunction("informTimeCritical", "informTimeCriticalType", Arrays.asList(getDataIns("InVal", "number", ifThenElse, 0)), Arrays.asList(new DataOutsAtomic("OutVal", "string")));
        ifThenElse.setThen(Arrays.asList(recommendShop));
        ifThenElse.setElse(Arrays.asList(informTimeCritical));
        ifThenElse.setDataOuts(Arrays.asList(new DataOuts("OutVal", "string", getDataOutsByIndex(recommendShop,0)+","+getDataOutsByIndex(informTimeCritical,0))));

        parallelF3F4.setParallelBody(Arrays.asList(new Section(Arrays.asList(informPassenger)), new Section(Arrays.asList(calculateTimeToGate, ifThenElse))));
        parallelF3F4.setDataOuts(Arrays.asList(new DataOuts("OutVal", "string", getDataOutsByIndex(ifThenElse,0))));
        parallelFor.setLoopBody(Arrays.asList(parallelF3F4));
        parallelFor.setDataOuts(Arrays.asList(new DataOuts("OutVal", "collection", getDataOutsByIndex(parallelF3F4,0))));

        // log
        AtomicFunction log = new AtomicFunction("log", "logType", Arrays.asList(getDataOuts("InVal", "collection", parallelFor, 0)), null);

        // Set all compounds as a sequence
        workflow.setWorkflowBody(Arrays.asList(getFlight, selectPassenger, parallelFor, log));

        // Validate workflow and write as YAML
        Utils.writeYaml(workflow, "gateChangeAlert.yaml", "schema.json");
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
