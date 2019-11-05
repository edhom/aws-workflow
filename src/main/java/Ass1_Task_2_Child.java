import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import net.schmizz.sshj.SSHClient;

import java.util.*;

/**
 * args[0]... InstanceType (String) i.e. "t2.large"
 * args[1]... Child ID i.e. "1"
 */
public class Ass1_Task_2_Child {

    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis();
        System.out.println("Retrieving public client IP from checkip.amazonaws.com...");
        String publicClientIP = GeneralUtils.getPublicIP();
        System.out.println("Public Client IP: " + publicClientIP);

        AmazonEC2Client ec2Client = EC2Utils.getClient();
        ec2Client = EC2Utils.getClient();

        //create new key pair
        String newKeyPairName = "KeyPair3.pem";
        EC2Utils.createKeyPair(ec2Client, newKeyPairName);

        //create security group and add permissions
        String newGroupName = "SecurityGroup3";
        EC2Utils.createSecurityGroup(ec2Client, newGroupName, "Security Group for Homework 01.");

        //allow SSH
        EC2Utils.addRuleToSecurityGroup(ec2Client, newGroupName, publicClientIP + "/32", 22, 22, "tcp");

        //launch instance
        String imageID = "ami-00aa4671cbf840d82";

        String instanceType = args[0];
        long launchStartTime = System.currentTimeMillis();
        RunInstancesResult runInstancesResult = EC2Utils.runInstance(ec2Client, imageID, instanceType, newKeyPairName, newGroupName);
        String instanceID = runInstancesResult.getReservation().getInstances().get(0).getInstanceId();
        //waiting for instance to be in status "running"
        EC2Utils.waitForInstanceState(ec2Client, instanceID, "running", 250, 600);

        long launchTime = System.currentTimeMillis() - launchStartTime;

        //Get public DNS and IP
        HashMap<String, String> publicDNSIP = EC2Utils.getPublicIPandDNS(ec2Client, instanceID);

        SSHClient sshClient = SSHUtils.connectSSH(publicDNSIP.get("DNS"), "ec2-user", newKeyPairName);

        //Install Java
        System.out.println("Installing Java:");
        long javaStartTime = System.currentTimeMillis();
        SSHUtils.executeCMD(sshClient, "sudo yum -y install java-1.8.0", 600);
        long javaTime = System.currentTimeMillis() - javaStartTime;

        //Upload Input File
        long uploadStartTime = System.currentTimeMillis();
        SSHUtils.SCPUpload(sshClient, "input_" + args[1] + ".csv", "/tmp");
        SSHUtils.SCPUpload(sshClient, "calc_fib.jar", "/tmp");
        long uploadTime = System.currentTimeMillis() - uploadStartTime;

        //Computation
        System.out.println("Computation:");
        long computationStartTime = System.currentTimeMillis();
        SSHUtils.executeCMD(sshClient, "java -jar /tmp/calc_fib.jar /tmp/input_" + args[1] + ".csv", 600);
        long computationTime = System.currentTimeMillis() - computationStartTime;

        //Download results
        long downloadStartTime = System.currentTimeMillis();
        SSHUtils.SCPDownload(sshClient,"/home/ec2-user/output.csv"  , "./output_"+ args[1] + ".csv");
        long downloadTime = System.currentTimeMillis() - downloadStartTime;

        System.out.println("\n--- Cleaning up ---");
        //clean-up
        EC2Utils.terminateInstance(ec2Client, instanceID);
        EC2Utils.deleteSecurityGroup(ec2Client, newGroupName);
        EC2Utils.deleteKeyPair(ec2Client, newKeyPairName);

        long measuredTotalTime = System.currentTimeMillis() - startTime;

        System.out.println("\n---------------------------");
        System.out.println("Finished Computation - TIME MEASUREMENTS(" + instanceType + "; Child ID: " + args[1] + "; VM ID:" + instanceID + ")");
        System.out.println("VM Startup: " + launchTime + " MS = " + launchTime/1000 + " S");
        System.out.println("Java Installation: " + javaTime + " MS = " + javaTime/1000 + " S");
        System.out.println("Upload: " + uploadTime + " MS = " + uploadTime/1000 + " S");
        System.out.println("Execution: " + computationTime + " MS = " + computationTime/1000 + " S");
        System.out.println("Download: " + downloadTime + " MS = " + downloadTime/1000 + " S");
        System.out.println("---------------------------");
        long sumTime = launchTime + javaTime + uploadTime + computationTime + downloadTime;
        System.out.println("Sum of Times: " + sumTime + " MS = " + sumTime/1000 + " S");
        System.out.println("Total Time Measured (i.e. +waiting time): " + measuredTotalTime + " MS = " + measuredTotalTime/1000 + " S");

        sshClient.disconnect();
        sshClient.close();

        return;
    }
}

