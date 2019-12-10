package Homework;

import Homework.EC2Utils;
import Homework.GeneralUtils;
import Homework.SSHUtils;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import net.schmizz.sshj.SSHClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;


public class Ass2_Task1 {
    @SuppressWarnings("Duplicates")
    public static void main(String[] args) throws InterruptedException, IOException {
        long startTime = System.currentTimeMillis();

        System.out.println("Retrieving public client IP from checkip.amazonaws.com...");
        String publicClientIP = GeneralUtils.getPublicIP();
        System.out.println("Public Client IP: " + publicClientIP);

        AmazonEC2Client ec2Client;
        ec2Client = EC2Utils.getClient();

        //create new key pair
        String newKeyPairName = "KeyPair8.pem";
        EC2Utils.createKeyPair(ec2Client, newKeyPairName);

        //create security group and add permissions
        String newGroupName = "SecurityGroup8";
        EC2Utils.createSecurityGroup(ec2Client, newGroupName, "Security Group for Homework 02.");

        //allow SSH
        EC2Utils.addRuleToSecurityGroup(ec2Client, newGroupName, publicClientIP + "/32", 22, 22, "tcp");

        //launch instance
        String imageID = "ami-00aa4671cbf840d82";

        String instanceType = args[0];
        long launchStartTime = System.currentTimeMillis();
        RunInstancesResult runInstancesResult = EC2Utils.runInstance(ec2Client, imageID, instanceType, newKeyPairName, newGroupName, null);
        String instanceID = runInstancesResult.getReservation().getInstances().get(0).getInstanceId();
        //waiting for instance to be in status "running"
        EC2Utils.waitForInstanceState(ec2Client, instanceID, "running", 250, 600);

        long launchTime = System.currentTimeMillis() - launchStartTime;

        //Get public DNS and IP
        HashMap<String, String> publicDNSIP = EC2Utils.getPublicIPandDNS(ec2Client, instanceID);

        SSHClient sshClient = SSHUtils.connectSSH(publicDNSIP.get("DNS"), "ec2-user", newKeyPairName);
        SSHUtils.executeCMD(sshClient, "pwd", 600);

        //upload CSV File
        SSHUtils.SCPUpload(sshClient, "input_full.csv", "/home/ec2-user");

        //Install Docker
        System.out.println("Installing Docker:");
        long dockerStartTime = System.currentTimeMillis();
        SSHUtils.executeCMD(sshClient, "sudo yum update -y", 600);
        SSHUtils.executeCMD(sshClient, "sudo amazon-linux-extras install docker", 600);
        //Homework.SSHUtils.executeCMD(sshClient, "y", 600);
        long dockerTime = System.currentTimeMillis() - dockerStartTime;

        //Docker start and login
        long dockerLoginStartTime = System.currentTimeMillis();
        SSHUtils.executeCMD(sshClient, "sudo service docker start", 600);
        List<String> dockerHubLoginData = GeneralUtils.loadDockerAccessDataFromConfig();
        String dockerLoginCommand = "sudo docker login --username='"+ dockerHubLoginData.get(0) + "'" + " --password='" + dockerHubLoginData.get(1) + "'";
        SSHUtils.executeCMD(sshClient, dockerLoginCommand, 600);
        long dockerLoginTime = System.currentTimeMillis() - dockerLoginStartTime;

        //Docker pull image
        long pullingImageStartTime = System.currentTimeMillis();
        String dockerPullCommand = "sudo docker pull " + dockerHubLoginData.get(0) + "/calc_fib:4";
        SSHUtils.executeCMD(sshClient, dockerPullCommand, 600);
        long pullingImageTime = System.currentTimeMillis() - pullingImageStartTime;

        //Docker execute jar File
        long runtimeImageStartTime = System.currentTimeMillis();
        String dockerExeCommand = "sudo docker run -v $(pwd):/src " + dockerHubLoginData.get(0) + "/calc_fib:4";
        SSHUtils.executeCMD(sshClient, dockerExeCommand, 600);
        long runtimeImage = System.currentTimeMillis() - runtimeImageStartTime;

        //Download result
        long downloadResultStartTime= System.currentTimeMillis();
        SSHUtils.SCPDownload(sshClient,"/home/ec2-user/output.csv"  , "./output.csv");
        long downloadResultTime = System.currentTimeMillis() - downloadResultStartTime;

        System.out.println("\n--- Cleaning up ---");
        //clean-up
        EC2Utils.terminateInstance(ec2Client, instanceID);
        EC2Utils.deleteSecurityGroup(ec2Client, newGroupName);
        EC2Utils.deleteKeyPair(ec2Client, newKeyPairName);

        long measuredTotalTime = System.currentTimeMillis() - startTime;

        System.out.println("\n---------------------------");
        System.out.println("Finished Computation - TIME MEASUREMENTS");
        System.out.println("VM Startup: " + launchTime + " MS = " + launchTime/1000 + " S");
        System.out.println("Docker Installation: " + dockerTime + " MS = " + dockerTime/1000 + " S");
        System.out.println("Docker start and login: " + dockerLoginTime + " MS = " + dockerLoginTime/1000 + " S");
        System.out.println("Pull from Docker Hub: " + pullingImageTime + " MS = " + pullingImageTime/1000 + " S");
        System.out.println("Runtime of Image: " + runtimeImage + " MS = " + runtimeImage/1000 + " S");
        System.out.println("Download result: " + downloadResultTime + " MS = " + downloadResultTime/1000 + " S");
        System.out.println("---------------------------");
        long sumTime = launchTime + dockerTime + pullingImageTime + runtimeImage + downloadResultTime;
        System.out.println("Sum of Times: " + sumTime + " MS = " + sumTime/1000 + " S");
        System.out.println("Total Time Measured (i.e. +waiting time): " + measuredTotalTime + " MS = " + measuredTotalTime/1000 + " S");

        sshClient.disconnect();
        sshClient.close();

    }
}
