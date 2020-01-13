package Project.Redis;

import Homework.EC2Utils;
import Homework.GeneralUtils;
import Homework.SSHUtils;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import net.schmizz.sshj.SSHClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class RedisClusterCreator {
    @SuppressWarnings("Duplicates")
    public static String create() throws InterruptedException, IOException {

        System.out.println("Retrieving public client IP from checkip.amazonaws.com...");
        String publicClientIP = GeneralUtils.getPublicIP();
        System.out.println("Public Client IP: " + publicClientIP);

        AmazonEC2Client ec2Client;
        ec2Client = EC2Utils.getClient();

        //create new key pair
        String newKeyPairName = "KeyPair43.pem";
        EC2Utils.createKeyPair(ec2Client, newKeyPairName);

        //create security group and add permissions
        String newGroupName = "SecurityGroup43";
        EC2Utils.createSecurityGroup(ec2Client, newGroupName, "Security Group for Homework 02.");

        //allow SSH
        EC2Utils.addRuleToSecurityGroup(ec2Client, newGroupName, null, 22, 22, "tcp");
        //allow Serving Redis Clients
        EC2Utils.addRuleToSecurityGroup(ec2Client, newGroupName, null, 6379, 6379, "tcp");
        //open data port
        EC2Utils.addRuleToSecurityGroup(ec2Client, newGroupName, null, 16379, 16379, "tcp");

        //launch instance
        String imageID = "ami-010fae13a16763bb4";

        String instanceType = "t2.micro";

        ArrayList<String> instanceIDs = new ArrayList<>();
        ArrayList<HashMap<String, String>> publicDNSIP = new ArrayList<>();

        //creating 6 instances
        for (int i = 0; i < 6; i++) {
            String userData = GeneralUtils.getUserDataClusterMode();
            RunInstancesResult runInstancesResult = EC2Utils.runInstance(ec2Client, imageID, instanceType, newKeyPairName, newGroupName, userData);
            instanceIDs.add(runInstancesResult.getReservation().getInstances().get(0).getInstanceId());
        }
        //waiting for all instances to be in state "running"
        for (int i = 0; i < 6; i++) {
            //waiting for instance i to be in status "running"
            EC2Utils.waitForInstanceState(ec2Client, instanceIDs.get(i), "running", 250, 600);
            //Get public DNS and IP
            System.out.println("I'm node " + i + " and this is my address...");
            publicDNSIP.add(EC2Utils.getPublicIPandDNS(ec2Client, instanceIDs.get(i)));
        }

        System.out.println("Waiting for initialization...");
        for (int i = 0; i < 6; i++) {
            EC2Utils.waitForInitialization(ec2Client, instanceIDs.get(i));
            System.out.println("Instance " + i + " initialized.");
        }

        System.out.println("Cluster creation in...");
        for (int i = 60; i > 0; i--) {
            System.out.println(i);
            TimeUnit.SECONDS.sleep(1);
        }

        // Create cluster
        SSHClient sshClient = SSHUtils.connectSSH(publicDNSIP.get(0).get("DNS"), "ec2-user", newKeyPairName);

        StringJoiner nodesJoiner = new StringJoiner(":6379 ");
        for (int i = 0; i < 6; i++) {
            nodesJoiner.add(publicDNSIP.get(i).get("IP"));
        }

        String createCluster = "(sleep 5; echo yes;) | redis-cli --cluster create " + nodesJoiner.toString() + ":6379 --cluster-replicas 1 | sleep 30";

        SSHUtils.executeCMD(sshClient, createCluster, 30);

        sshClient.close();

        System.out.println("\n---------------------------");
        System.out.println("Finished Cluster Creation. Have fun!");
        return publicDNSIP.get(0).get("DNS") + ":6379";
    }
}
