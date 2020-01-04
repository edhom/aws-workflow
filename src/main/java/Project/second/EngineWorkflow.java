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
import com.dps.afcl.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;

public class EngineWorkflow {

    public static HashMap<String,String> storage = new HashMap<>();
    public static void parseWorkflow(String yamlFile){
        String path = "C:\\Users\\geige\\Documents\\3_Semester\\02_Verteilte Systeme\\distributed_systems\\src\\main\\resources\\schema.json";
        Workflow workflow = Utils.readYAML(yamlFile, path);

        List<Function> functions = workflow.getWorkflowBody();

        parseSequence(functions, 0);

        System.out.println("");
        System.out.println("");
        System.out.println("");
        for(Map.Entry<String, String> j : storage.entrySet()){
            System.out.println(j.getKey() + "  " + j.getValue());
        }
    }

    public static void parseSequence(List<Function> functions, Integer index){
        for(Function func : functions){
            parseFunction(func, index);
        }
    }

    public static void parseFunction(Function function, Integer index){
        String type = function.getClass().toString();

        if(type.equals("class com.dps.afcl.functions.Parallel")){
            parseParallel((Parallel) function);
        }
        else if(type.equals("class com.dps.afcl.functions.ParallelFor")){
            parseParallelFor((ParallelFor) function);
        }
        else if(type.equals("class com.dps.afcl.functions.AtomicFunction")){
            parseAtomicFunction((AtomicFunction) function, index);
        }
        else{
            System.out.println("Shouldn't be here");
        }
    }

    public static void parseParallel(Parallel parallel){
        List<Thread> threads = new ArrayList<>();

        parallel.getParallelBody().forEach(section -> {

            Thread t = new Thread(() -> parseSequence(section.getSection(), 0));
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

        String input = storage.get(parallelFor.getDataIns().get(0).getSource());

        JSONArray inputArray = stringToJSONArray(input);
        List<Function> loopFunctions = parallelFor.getLoopBody();
        AtomicFunction atomicFunction = (AtomicFunction) loopFunctions.get(0);
        Integer inputSize = null;

        if(parallelFor.getLoopCounter().getTo().equals("inputSize")){
            inputSize = inputArray.size();
        }
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < inputSize; i += 1) {
            int finalI = i;
            Thread t = new Thread(() -> {

                JSONObject object = (JSONObject) inputArray.get(finalI);
                String part = atomicFunction.getName() + "/" + atomicFunction.getDataIns().get(0).getName() + "/" + finalI;
                //System.out.println("QQQQQQQQQQQQQQQQ"+blockInputSource);
                storage.put(part, object.toJSONString());

                atomicFunction.setDataIns(Arrays.asList(new DataIns(part, "Collection", part)));
                parseSequence(loopFunctions, finalI);

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

        String[] merge = new String[inputSize];
        for(int i = 0; i < inputSize; i++){
            merge[i] = storage.get(parallelFor.getDataOuts().get(0).getSource()+"/"+i);
        }
        storage.put(parallelFor.getName()+"/"+parallelFor.getDataOuts().get(0).getName(), Arrays.toString(merge));
    }

    public static void parseAtomicFunction(AtomicFunction atomicFunction, Integer index){
        Integer i = 0;
        String jsonInput = null;

        //System.out.println("!!!!!!!!!!!" + valueStore.valueStore.get("f2_checkMatches/InVal3/" + index));

        //jsonInput = valueStore.valueStore.get(path);
        if(atomicFunction.getName().equals("f2_checkMatches") || atomicFunction.getName().equals("f6_optimalRideRequest")){
            jsonInput = storage.get(atomicFunction.getDataIns().get(0).getSource());
        }

        else if(!atomicFunction.getName().equals("f1_RideRequest")){
            //System.out.println("f333333333333333333"+atomicFunction.getDataIns().get(0).getSource());
            jsonInput = storage.get(atomicFunction.getDataIns().get(0).getSource()+"/"+ index);
        }

        //System.out.println("[" + atomicFunction.getName() + "] input: " + jsonInput);
        String output = invokeFunction(atomicFunction.getName(), jsonInput);
        //System.out.println("[" + atomicFunction.getName() + "] output: " + output);

        String outputName = atomicFunction.getDataOuts().get(0).getName();
        if(!atomicFunction.getName().equals("f1_RideRequest")) {
            storage.put(atomicFunction.getName() + "/" + outputName + "/" + index, output);
        }
        else{
            storage.put(atomicFunction.getName() + "/" + outputName, output);
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

}
