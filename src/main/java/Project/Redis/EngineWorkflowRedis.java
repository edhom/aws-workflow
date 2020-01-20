package Project.Redis;

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
import com.dps.afcl.functions.AtomicFunction;
import com.dps.afcl.functions.Parallel;
import com.dps.afcl.functions.ParallelFor;
import com.dps.afcl.functions.objects.DataIns;
import com.dps.afcl.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.nio.charset.Charset;
import java.util.*;

@SuppressWarnings("Duplicates")
public class EngineWorkflowRedis {

    public static RedisHashMap storage;

    public static void initializeStorage(String dns) {
        storage = new RedisHashMap(dns);
    }

    public static void freeStorage() {
        storage.close();
    }

    //public static HashMap<String,String> storage = new HashMap<>();
    public static void parseWorkflow(String yamlFile) throws ParseException {
        String path = "/Users/ericdhom/Desktop/Uni/Semester3/VerteilteSysteme/Repo/src/main/resources/schema.json";
        Workflow workflow = Utils.readYAML(yamlFile, path);

        List<Function> functions = workflow.getWorkflowBody();

        System.out.println("");System.out.println("---------------- Parsing the workflow ----------------");System.out.println("");
        parseSequence(functions, 0);
    }

    public static void parseSequence(List<Function> functions, Integer index) throws ParseException {
        for(Function func : functions){
            parseFunction(func, index);
        }
    }

    public static void parseFunction(Function function, Integer index) throws ParseException {
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
            System.out.println("Whaaat, don't know this type");
        }
    }

    public static void parseParallel(Parallel parallel){
        List<Thread> threads = new ArrayList<>();

        parallel.getParallelBody().forEach(section -> {

            Thread t = new Thread(() -> {
                try {
                    parseSequence(section.getSection(), 0);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
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

    public static void parseParallelFor(ParallelFor parallelFor) throws ParseException {

        String input = storage.get(parallelFor.getDataIns().get(0).getSource());

        JSONArray inputArray = convertToJSONArray(input);
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
                storage.put(part, object.toJSONString());

                atomicFunction.setDataIns(Arrays.asList(new DataIns(part, "Collection", part)));
                try {
                    parseSequence(loopFunctions, finalI);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

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
        String input = null;


        if(atomicFunction.getName().equals("f2_checkMatches") || atomicFunction.getName().equals("f6_optimalRideRequest")){
            input = storage.get(atomicFunction.getDataIns().get(0).getSource());
        }

        else if(!atomicFunction.getName().equals("f1_RideRequest")){
            input = storage.get(atomicFunction.getDataIns().get(0).getSource()+"/"+ index);
        }

        System.out.println(atomicFunction.getName() + " input: " + input);

        String output = invokeFunction(atomicFunction.getName(), input);

        String outputName = null;
        if(!atomicFunction.getName().equals("f1_RideRequest")) {
            outputName = atomicFunction.getName() + "/" + atomicFunction.getDataOuts().get(0).getName()  + "/" + index;
        }
        else{
            outputName = atomicFunction.getName() + "/" + atomicFunction.getDataOuts().get(0).getName();
        }

        storage.put(outputName, output);
        System.out.println(atomicFunction.getName() + " output: " + output);

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

        InvokeResult invokeResult = awsLambda.invoke(request);
        String resultString = new String(invokeResult.getPayload().array(), Charset.forName("UTF-8"));
        return resultString;
    }

    public static JSONArray convertToJSONArray(String jsonString) throws ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = (JSONArray) jsonParser.parse(jsonString);
        return jsonArray;
    }

}
