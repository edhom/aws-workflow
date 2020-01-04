package Project.second;

import Homework.GeneralUtils;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.dps.afcl.Function;
import com.dps.afcl.Workflow;
import com.dps.afcl.functions.*;
import com.dps.afcl.functions.objects.DataIns;
import com.dps.afcl.functions.objects.dataflow.DataInsDataFlow;
import com.dps.afcl.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;

public class EngineWorkflow {
    public static ValueStore valueStore = new ValueStore();
    public static void parseWorkflow(String yamlFile){
        String path = "C:\\Users\\geige\\Documents\\3_Semester\\02_Verteilte Systeme\\distributed_systems\\src\\main\\resources\\schema.json";
        Workflow workflow = Utils.readYAML(yamlFile, path);

        List<Function> functions = workflow.getWorkflowBody();

        executeSequence(functions, 0, "");

        System.out.println("");
        System.out.println("");
        System.out.println("");
        for(Map.Entry<String, String> j : valueStore.valueStore.entrySet()){
            System.out.println(j.getKey() + "  " + j.getValue());
        }
    }

    public static void executeSequence(List<Function> functions, Integer index, String path){
        for(Function func : functions){
            parseFunction(func, index, path);
        }
    }

    public static void parseFunction(Function function, Integer index, String path){
        String type = function.getClass().toString();

        if(type.equals("class com.dps.afcl.functions.Parallel")){
            parseParallel((Parallel) function);
        }
        else if(type.equals("class com.dps.afcl.functions.ParallelFor")){
            parseParallelFor((ParallelFor) function);
        }
        else if(type.equals("class com.dps.afcl.functions.AtomicFunction")){
            parseAtomicFunction((AtomicFunction) function, index, path);
        }
        else{
            System.out.println("Shouldn't be here");
        }
    }

    public static void parseParallel(Parallel parallel){
        System.out.println(parallel.getDataIns().get(0).getSource());
        List<Thread> threads = new ArrayList<>();

        parallel.getParallelBody().forEach(section -> {

            Thread t = new Thread(() -> executeSequence(section.getSection(), 0, ""));
            threads.add(t);
            t.start();
        });

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void parseParallelFor(ParallelFor parallelFor){
        DataInsDataFlow input = parallelFor.getDataIns().get(0);

        String dataValue = valueStore.getValue(input.getSource());

        JSONArray inputCollection = stringToJSONArray(dataValue);
        List<Function> loopBody = parallelFor.getLoopBody();
        Integer loopBodySize = loopBody.size();
        String firstInputSource = inputToSource(loopBody.get(0), 0);
        System.out.println("firstinputsource " + firstInputSource );

        Integer to = Integer.parseInt(parallelFor.getLoopCounter().getTo());
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < to; i += 1) {
            int finalI = i;
            Thread t = new Thread(() -> {
                JSONObject object = (JSONObject) inputCollection.get(finalI);
                String blockInputSource = inputToSource(loopBody.get(0), 0) + "/" + finalI;
                //System.out.println("QQQQQQQQQQQQQQQQ"+blockInputSource);
                valueStore.addValue(blockInputSource, collectionToJSON(object));

                setInput(loopBody.get(0), Collections.singletonList(new DataIns(blockInputSource, "Collection", blockInputSource)));
                executeSequence(loopBody, finalI, blockInputSource);

            });
            threads.add(t);
            t.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("OUTPUT"+parallelFor.getDataOuts().get(0).getSource());
        String[] merge = new String[to];
        for(int i = 0; i < to; i++){
            merge[i] = valueStore.getValue(parallelFor.getDataOuts().get(0).getSource()+"/"+i);
        }
        valueStore.addValue(parallelFor.getName()+"/"+parallelFor.getDataOuts().get(0).getName(), Arrays.toString(merge));
        System.out.println("--------------------------------");
        System.out.println(Arrays.toString(merge));
        System.out.println("--------------------------------");
    }

    public static void parseAtomicFunction(AtomicFunction atomicFunction, Integer index, String path){
        Integer i = 0;
        String jsonInput = null;

        //System.out.println("!!!!!!!!!!!" + valueStore.valueStore.get("f2_checkMatches/InVal3/" + index));

        //jsonInput = valueStore.valueStore.get(path);
        if(atomicFunction.getName().equals("f2_checkMatches") || atomicFunction.getName().equals("f6_optimalRideRequest")){
            jsonInput = valueStore.valueStore.get(atomicFunction.getDataIns().get(0).getSource());
        }

        else if(atomicFunction.getName().equals("f3_CalcProfit") || atomicFunction.getName().equals("f4_calculateOverheadKM") || atomicFunction.getName().equals("f5_OverheadInTime") || atomicFunction.getName().equals("f7_OptimalPickUp")|| atomicFunction.getName().equals("f8_informPassenger") || atomicFunction.getName().equals("f9_InformDriver") || atomicFunction.getName().equals("f10_logInDatabase")){
            //System.out.println("f333333333333333333"+atomicFunction.getDataIns().get(0).getSource());
            jsonInput = valueStore.getValue(atomicFunction.getDataIns().get(0).getSource()+"/"+ index);
        }

        System.out.println("[" + atomicFunction.getName() + "] input: " + jsonInput);
        String output = invokeFunction(atomicFunction.getName(), jsonInput);
        System.out.println("[" + atomicFunction.getName() + "] output: " + output);

        String outputName = atomicFunction.getDataOuts().get(0).getName();
        if(!atomicFunction.getName().equals("f1_RideRequest")) {
            valueStore.addValue(atomicFunction.getName() + "/" + outputName + "/" + index, output);
        }
        else{
            valueStore.addValue(atomicFunction.getName() + "/" + outputName, output);
        }
        i++;
    }

    public static String invokeFunction(String functionName, String input){
        // Get AWS Client
        BasicAWSCredentials awsCredentials = GeneralUtils.loadCredentialsFromConfig();
        AWSLambda awsLambda = AWSLambdaClientBuilder
                .standard()
                .withRegion(Regions.EU_CENTRAL_1)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();

        InvokeRequest request = new InvokeRequest()
                .withFunctionName(functionName)
                .withPayload(input);

        InvokeResult result = awsLambda.invoke(request);
        StringBuilder sb = new StringBuilder();
        while (result.getPayload().hasRemaining()){
            sb.append((char)result.getPayload().get());
        }
        return sb.toString();
    }

    public static JSONArray stringToJSONArray(String jsonString) {
        try {
            JSONParser jsonParser = new JSONParser();
            return (JSONArray) jsonParser.parse(jsonString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static void setInput(Function function, List<DataIns> input) {
        if (function instanceof AtomicFunction) {
            AtomicFunction f = (AtomicFunction) function;
            f.setDataIns(input);
        } else if (function instanceof CompoundSequential) {
            CompoundSequential f = (CompoundSequential) function;
            f.setDataIns(input);
        } else {
            System.err.println("Not supported");
        }
    }

    private static String inputToSource(Function function, int index) {
        if (function instanceof AtomicFunction) {
            AtomicFunction atomicFunction = (AtomicFunction) function;
            return atomicFunction.getName() + "/" + atomicFunction.getDataIns().get(index).getName();
        } else if (function instanceof CompoundSequential) {
            CompoundSequential compoundSequential = (CompoundSequential) function;
            return compoundSequential.getName() + "/" + compoundSequential.getDataIns().get(index).getName();
        } else if (function instanceof CompoundParallel) {
            CompoundParallel compoundParallel = (CompoundParallel) function;
            return compoundParallel.getName() + "/" + compoundParallel.getDataIns().get(index).getName();
        }
        System.err.println("Not supported");
        return null;
    }

    public static String collectionToJSON(JSONObject input) {
        return input.toJSONString();
    }

}
