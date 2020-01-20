package Homework;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
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
        return sb.toString();
    }

    public static String getUserDataNormalNode() {
        String userData = "";
        userData += "#!/bin/bash\n";
        userData += "echo \"#!/bin/bash\nyum -y update\nsudo yum -y install gcc make\ncd /usr/local/src \nsudo wget http://download.redis.io/redis-stable.tar.gz\nsudo tar xvzf redis-stable.tar.gz\nsudo rm -f redis-stable.tar.gz\ncd redis-stable\nsudo make distclean\nsudo make\nsudo yum install -y tcl\nsudo cp src/redis-server /usr/local/bin/\nsudo cp src/redis-cli /usr/local/bin/\nsudo cp src/redis-benchmark /usr/local/bin\" >> /home/ec2-user/setup_node.sh\n";
        userData += "chown ec2-user:ec2-user /home/ec2-user/setup_node.sh\n";
        userData += "chmod +x /home/ec2-user/setup_node.sh\n";
        userData += "/home/ec2-user/setup_node.sh\n";
        userData += "/usr/local/bin/redis-server\n";
        return encodeBase64(userData);
    }
/*
    public static String getUserDataClusterMode() {
        String userData = "";
        userData += "#!/bin/bash\n";
        userData += "sudo service awslogs stop\n";
        userData += "sudo sed -i -e '$i \\echo never > /sys/kernel/mm/transparent_hugepage/enabled &\\n' /etc/rc.local\n";
        userData += "sudo sh -c \"echo never > /sys/kernel/mm/transparent_hugepage/enabled\"\n";
        userData += "sudo sh -c \"echo 'vm.overcommit_memory = 1' >> /etc/sysctl.conf\"";
        userData += "sudo sysctl vm.overcommit_memory=1\n";
        userData += "echo \"protected-mode no\nport 6379\ncluster-enabled yes\ncluster-config-file nodes.conf\ncluster-node-timeout 5000\nappendonly yes\" >> /usr/local/bin/redis.conf\n";
        userData += "echo \"#!/bin/bash\nyum -y update\nsudo yum -y install gcc make\ncd /usr/local/src \nsudo wget http://download.redis.io/redis-stable.tar.gz\nsudo tar xvzf redis-stable.tar.gz\nsudo rm -f redis-stable.tar.gz\ncd redis-stable\nsudo make distclean\nsudo make\nsudo yum install -y tcl\nsudo cp src/redis-server /usr/local/bin/\nsudo cp src/redis-cli /usr/local/bin/\nsudo cp src/redis-benchmark /usr/local/bin\" >> /home/ec2-user/setup_node.sh\n";
        userData += "chown ec2-user:ec2-user /home/ec2-user/setup_node.sh\n";
        userData += "chmod +x /home/ec2-user/setup_node.sh\n";
        userData += "/home/ec2-user/setup_node.sh\n";
        userData += "/usr/local/bin/redis-server /usr/local/bin/redis.conf\n";
        return encodeBase64(userData);
    }
*/
    public static String getUserDataClusterMode() {
        String userData = "";
        userData += "#!/bin/bash\n";
        userData += "echo \"#!/bin/bash\nyum -y update\nsudo yum -y install gcc make\ncd /usr/local/src \nsudo wget http://download.redis.io/redis-stable.tar.gz\nsudo tar xvzf redis-stable.tar.gz\nsudo rm -f redis-stable.tar.gz\ncd redis-stable\nsudo make distclean\nsudo make\nsudo yum install -y tcl\nsudo cp src/redis-server /usr/local/bin/\nsudo cp src/redis-cli /usr/local/bin/\n\" >> /home/ec2-user/setup_node.sh\n";
        userData += "echo \"" +
                "port 6379\n" +
                "cluster-enabled yes\n" +
                "cluster-config-file nodes.conf\n" +
                "cluster-node-timeout 5000\n" +
                "appendonly yes\n" +
                "cluster-announce-ip \"$(curl -s http://checkip.amazonaws.com)\"\n" +
                "requirepass \"supersecret\"\n" +
                "masterauth \"supersecret\"\"" +
                " >> /home/ec2-user/redis-cluster.cfg\n";
        userData += "chown ec2-user:ec2-user /home/ec2-user/setup_node.sh\n";
        userData += "chown ec2-user:ec2-user /home/ec2-user/redis-cluster.cfg\n";
        userData += "chmod +x /home/ec2-user/setup_node.sh\n";
        userData += "chmod +x /home/ec2-user/redis-cluster.cfg\n";
        userData += "/home/ec2-user/setup_node.sh\n";
        userData += "/usr/local/bin/redis-server /home/ec2-user/redis-cluster.cfg\n";
        return encodeBase64(userData);
    }


    public static String encodeBase64(String input) {
        String base64UserData = null;
        try {
            base64UserData = new String(Base64.encodeBase64(input.getBytes("UTF-8")), "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            System.out.println(uee.getMessage());
        }
        return base64UserData;
    }

    public static String readClusterDNS(String filename) {
        String clusterDNS = "";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filename));
            clusterDNS = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return clusterDNS;
    }

    public static void writeClusterDNS(String filename, String clusterDNS) {
        File dnsFile = new File(filename);
        try {
            FileWriter fw = new FileWriter(dnsFile, false);
            fw.write(clusterDNS);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
