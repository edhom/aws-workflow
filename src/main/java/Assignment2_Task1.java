import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import net.schmizz.sshj.SSHClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;


public class Assignment2_Task1 {
    @SuppressWarnings("Duplicates")
    public static void main(String[] args) throws InterruptedException, IOException {
        long startTime = System.currentTimeMillis();
        System.out.println("Retrieving public client IP from checkip.amazonaws.com...");
        String publicClientIP = GeneralUtils.getPublicIP();
        System.out.println("Public Client IP: " + publicClientIP);

        AmazonEC2Client ec2Client;
        ec2Client = EC2Utils.getClient();

        //create new key pair
        String newKeyPairName = "KeyPair6.pem";
        EC2Utils.createKeyPair(ec2Client, newKeyPairName);

        //create security group and add permissions
        String newGroupName = "SecurityGroup6";
        EC2Utils.createSecurityGroup(ec2Client, newGroupName, "Security Group for Homework 02.");

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
        SSHUtils.executeCMD(sshClient, "pwd", 600);

        //upload CSV File
        SSHUtils.SCPUpload(sshClient, "input_full.csv", "/home/ec2-user");

        //Install Docker
        System.out.println("Installing Docker:");
        long javaStartTime = System.currentTimeMillis();
        SSHUtils.executeCMD(sshClient, "sudo yum update -y", 600);
        SSHUtils.executeCMD(sshClient, "sudo amazon-linux-extras install docker", 600);
        SSHUtils.executeCMD(sshClient, "y", 600);
        SSHUtils.executeCMD(sshClient, "sudo service docker start", 600);
        long javaTime = System.currentTimeMillis() - javaStartTime;

        //Docker login
        List<String> dockerHubLoginData = GeneralUtils.loadDockerAccessDataFromConfig();
        String dockerLoginCommand = "sudo docker login --username "+ dockerHubLoginData.get(0) + " --password " + dockerHubLoginData.get(1);
        SSHUtils.executeCMD(sshClient, dockerLoginCommand, 600);

        //Docker pull image
        String dockerPullCommand = "sudo docker pull " + dockerHubLoginData.get(0) + "/calc_fib:2";
        SSHUtils.executeCMD(sshClient, dockerPullCommand, 600);


        //Docker execute jar File
        String dockerExeCommand = "sudo docker run -v $(pwd):/src -it " + dockerHubLoginData.get(0) + "/calc_fib:2";

        SSHUtils.executeCMD(sshClient, dockerExeCommand, 600);

    }
}
