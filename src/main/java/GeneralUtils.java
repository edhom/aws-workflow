import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.lang.Object;
import java.net.PasswordAuthentication;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


public class GeneralUtils {

    public static BasicAWSCredentials loadCredentialsFromConfig() {
        //Initialize variables
        InputStream config = null;
        String access_key_id = "";
        String aws_secret_access_key = "";

        try
        {
            // load a awsconfig file
            config = new FileInputStream("awsconfig");

            if (config == null) {
                System.out.println("Unable to find awsconfig file. Exiting program...");
                System.exit(1);
            }

            Properties prop = new Properties();

            prop.load(config);

            //get id and secret
            access_key_id = prop.getProperty("aws_access_key_id");
            aws_secret_access_key = prop.getProperty("aws_secret_access_key");

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return new BasicAWSCredentials(access_key_id, aws_secret_access_key);
    }

    public static List<String> loadDockerAccessDataFromConfig() {
        //Initialize variables
        InputStream config = null;
        String access_key_id = "";
        String aws_secret_access_key = "";
        List<String> returnData = new ArrayList();

        try
        {
            // load a awsconfig file
            config = new FileInputStream("dockerconfig");

            if (config == null) {
                System.out.println("Unable to find dockerconfig file. Exiting program...");
                System.exit(1);
            }

            Properties prop = new Properties();

            prop.load(config);

            //get id and secret
            access_key_id = prop.getProperty("dockerHub_username");
            aws_secret_access_key = prop.getProperty("dockerHub_password");
            returnData.add(access_key_id);
            returnData.add(aws_secret_access_key);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return returnData;
    }

    public static String getPublicIP()
    {
        String publicIP = "";
        try {
            URL awscheckip = new URL("https://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    awscheckip.openStream()));

            publicIP = in.readLine();
            System.out.println("IP successfully retrieved...");
        }
        catch (Exception e)
        {
            System.out.println("Error while trying to get client public IP address. IP set to '0.0.0.0'.");
            publicIP = "0.0.0.0";
        }

        return publicIP;
    }

    public static void splitCSV(String filePath, String outputName, int fragments, int splitAfter){
        BufferedReader br = null;
        String line = "";
        ArrayList<String> fullCSV = new ArrayList<String>();

        try
        {
            br = new BufferedReader(new FileReader(filePath));
            while ((line = br.readLine()) != null) {
                fullCSV.add(line);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        int allLines = fullCSV.size();
        int fromIndex = 0;
        int toIndex = splitAfter;
        int currentFragments = 0;

        ArrayList<ArrayList<String>> splitCSV = new ArrayList<ArrayList<String>>();

        while(fromIndex < allLines && toIndex < allLines && currentFragments < fragments){
            splitCSV.add((ArrayList<String>)fullCSV.subList(fromIndex, toIndex));
            fromIndex = toIndex+1;
            toIndex = toIndex + splitAfter;
            currentFragments++;
        }

        if(fromIndex < allLines)
        {
            splitCSV.add((ArrayList<String>)fullCSV.subList(fromIndex, allLines));
        }
    }

    public static String ArrayToString(BigInteger[] arr) {
        StringJoiner join = new StringJoiner(" ");
        for (BigInteger x : arr) {
            join.add(x.toString());
        }
        return join.toString();
    }

}
