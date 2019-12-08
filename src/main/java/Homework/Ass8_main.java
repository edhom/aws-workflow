package Homework;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import net.schmizz.sshj.SSHClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;


public class Ass8_main {
    @SuppressWarnings("Duplicates")
    public static void main(String[] args) throws InterruptedException, IOException {
        long startTime = System.currentTimeMillis();

        System.out.println("Retrieving public client IP from checkip.amazonaws.com...");
        String publicClientIP = GeneralUtils.getPublicIP();
        System.out.println("Public Client IP: " + publicClientIP);

        AmazonEC2Client ec2Client;
        ec2Client = EC2Utils.getClient();

        //create new key pair
        String newKeyPairName = "KeyPair16.pem";
        EC2Utils.createKeyPair(ec2Client, newKeyPairName);

        //create security group and add permissions
        String newGroupName = "SecurityGroup16";
        EC2Utils.createSecurityGroup(ec2Client, newGroupName, "Security Group for Homework 02.");

        //allow SSH
        EC2Utils.addRuleToSecurityGroup(ec2Client, newGroupName, publicClientIP + "/32", 22, 22, "tcp");

        //launch instance
        String imageID = "ami-010fae13a16763bb4";

        String instanceType = args[0];
        String userData = GeneralUtils.getUserDataNormalNode();
        long launchStartTime = System.currentTimeMillis();
        RunInstancesResult runInstancesResult = EC2Utils.runInstance(ec2Client, imageID, instanceType, newKeyPairName, newGroupName, userData);
        String instanceID = runInstancesResult.getReservation().getInstances().get(0).getInstanceId();
        //waiting for instance to be in status "running"
        EC2Utils.waitForInstanceState(ec2Client, instanceID, "running", 250, 600);

        long launchTime = System.currentTimeMillis() - launchStartTime;

        //Get public DNS and IP
        HashMap<String, String> publicDNSIP = EC2Utils.getPublicIPandDNS(ec2Client, instanceID);


        System.out.println("\n--- Cleaning up ---");

        long measuredTotalTime = System.currentTimeMillis() - startTime;

        System.out.println("\n---------------------------");
        System.out.println("Finished Computation - TIME MEASUREMENTS");
        System.out.println("VM Startup: " + launchTime + " MS = " + launchTime/1000 + " S");

    }
}

