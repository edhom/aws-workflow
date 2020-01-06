package Homework;

import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.rds.model.IPRange;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class EC2Utils {

    private static AmazonEC2Client ec2Client = null;

    public static AmazonEC2Client getClient()
    {
        //if client already exists try to run a simple command to see if the connection works
        if(ec2Client != null)
        {
            try
            {
                ec2Client.describeInstanceStatus();
            }
            //if client got shutdown or disconnected - reconnect
            catch (IllegalStateException e)
            {
                ec2Client = new AmazonEC2Client(GeneralUtils.loadCredentialsFromConfig());
            }
            //if client has no connection
            catch (Exception e)
            {
                System.out.println("There was a problem connecting with amazonaws.com. Please make sure you have an active internet connection.");
            }
        }
        else
        {
            ec2Client = new AmazonEC2Client(GeneralUtils.loadCredentialsFromConfig());
        }
        ec2Client.setEndpoint("ec2.eu-central-1.amazonaws.com");

        return ec2Client;
    }

    public static KeyPair createKeyPair(AmazonEC2Client ec2Client, String keyName){

        CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest();
        createKeyPairRequest.withKeyName(keyName);

        CreateKeyPairResult createKeyPairResult = null;
        try {
            createKeyPairResult = ec2Client.createKeyPair(createKeyPairRequest);
            System.out.println("Key Pair '" + keyName + "' successfully created...");

            KeyPair keyPair = createKeyPairResult.getKeyPair();

            //create keypair file, needed for ssh access
            String privateKey = keyPair.getKeyMaterial();
            File keyFile;
            //if UNIX, set Posix File Permissions to 600
            if (!System.getProperty("os.name").startsWith("Windows")) {
                Set<PosixFilePermission> ownerWritable = PosixFilePermissions.fromString("rw-------");
                FileAttribute<?> permissions = PosixFilePermissions.asFileAttribute(ownerWritable);
                Path path = Paths.get(keyName);
                keyFile = Files.createFile(path, permissions).toFile();
            } else {
                keyFile = new File(keyName);
            }

            FileWriter fw = new FileWriter(keyFile);
            fw.write(privateKey);
            fw.close();
        }
        catch (AmazonEC2Exception e) {
            System.out.println(e.getErrorMessage());
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
        if(createKeyPairResult != null)
            return createKeyPairResult.getKeyPair();
        else
            return null;
    }

    public static DeleteKeyPairResult deleteKeyPair(AmazonEC2Client ec2Client, String keyName){

        DeleteKeyPairRequest deleteKeyPairRequest = new DeleteKeyPairRequest();
        deleteKeyPairRequest.withKeyName(keyName);

        DeleteKeyPairResult deleteKeyPairResult = null;

        try {
            deleteKeyPairResult = ec2Client.deleteKeyPair(deleteKeyPairRequest);
            System.out.println("Key Pair '" + keyName + "' successfully deleted...");
        }
        catch (AmazonEC2Exception e) {
            System.out.println(e.getErrorMessage());
        }

        return deleteKeyPairResult;
    }

    public static CreateSecurityGroupResult createSecurityGroup(AmazonEC2Client ec2Client, String groupName, String description) {
        CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest();
        createSecurityGroupRequest.withGroupName(groupName).withDescription(description);

        CreateSecurityGroupResult createSecurityGroupResult = null;
        try {
            createSecurityGroupResult = ec2Client.createSecurityGroup(createSecurityGroupRequest);
            System.out.println("Group '" + groupName + "' successfully created...");
        }
        catch (AmazonEC2Exception e) {
            System.out.println(e.getErrorMessage());
        }

        return createSecurityGroupResult;
    }

    public static DeleteSecurityGroupResult deleteSecurityGroup(AmazonEC2Client ec2Client, String groupName) {

        DeleteSecurityGroupRequest request = new DeleteSecurityGroupRequest()
                .withGroupName(groupName);

        DeleteSecurityGroupResult deleteSecurityGroupResult = null;
        try {
            deleteSecurityGroupResult = ec2Client.deleteSecurityGroup(request);
            System.out.println("Group '" + groupName + "' successfully deleted...");
        }
        catch (AmazonEC2Exception e) {
            System.out.println(e.getErrorMessage());
        }

        return deleteSecurityGroupResult;
    }

    public static AuthorizeSecurityGroupIngressResult addRuleToSecurityGroup(AmazonEC2Client ec2Client, String groupName, String ipRangeString, int fromPort, int toPort, String protocol) {
        IpPermission ipPermission = new IpPermission();

        IpRange ip_range = new IpRange().withCidrIp("0.0.0.0/0");
        Ipv6Range ipv6_range = new Ipv6Range().withCidrIpv6("::/0");

        IpPermission ip_perm = new IpPermission()
                .withIpProtocol(protocol)
                .withToPort(fromPort)
                .withFromPort(toPort)
                .withIpv4Ranges(ip_range)
                .withIpv6Ranges(ipv6_range);

        AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest = new AuthorizeSecurityGroupIngressRequest();

        authorizeSecurityGroupIngressRequest.withGroupName(groupName)
                .withIpPermissions(ip_perm);

        AuthorizeSecurityGroupIngressResult authorizeSecurityGroupIngressResult = null;
        try {
            authorizeSecurityGroupIngressResult = ec2Client.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
            System.out.println("Permission (iprange:" + "0.0.0.0/0" + "; from:" + fromPort + "; to:" + toPort + "; " + protocol + ") successfully added to group '" + groupName + "'...");
        }
        catch (AmazonEC2Exception e) {
            System.out.println(e.getErrorMessage());
        }

        return authorizeSecurityGroupIngressResult;
    }

    public static RunInstancesResult runInstance(AmazonEC2Client ec2Client, String imageID, String instanceType, String keyPairName, String securityGroupName, String userData) {
        RunInstancesRequest runInstancesRequest =
                new RunInstancesRequest();

        runInstancesRequest.withImageId(imageID)
                .withInstanceType(instanceType)
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName(keyPairName)
                .withSecurityGroups(securityGroupName)
                .withUserData(userData);


        RunInstancesResult runInstancesResult = null;
        try {
            runInstancesResult = ec2Client.runInstances(runInstancesRequest);
            System.out.println("Instance (imageID:" + imageID + ";instanceType:" + instanceType.toString() + ";keyPair:" + keyPairName + ";securityGroup:" + securityGroupName + ") successfully created and launched...");
        }
        catch (AmazonEC2Exception e) {
            System.out.println(e.getErrorMessage());
        }

        return runInstancesResult;
    }

    public static TerminateInstancesResult terminateInstance(AmazonEC2Client ec2Client, String instanceID) {
        TerminateInstancesRequest terminateInstancesRequest =
                new TerminateInstancesRequest();

        terminateInstancesRequest.withInstanceIds(instanceID);

        TerminateInstancesResult terminateInstancesResult = null;
        try {
            terminateInstancesResult = ec2Client.terminateInstances(terminateInstancesRequest);
            System.out.println("Instance (ID:" + instanceID + ") successfully terminated...");
        }
        catch (AmazonEC2Exception e) {
            System.out.println(e.getErrorMessage());
        }

        return terminateInstancesResult;
    }

    public static void waitForInstanceState(AmazonEC2Client ec2Client, String instanceID, String instanceState, int loopWaitTimeMS, int maxWaitTimeS) throws InterruptedException {

        System.out.println("Waiting for Instance (ID:" + instanceID + ") to be in state '" + instanceState + "'...");
        long startTime = System.currentTimeMillis();
        long endTime = maxWaitTimeS*1000 + System.currentTimeMillis();

        String currentInstanceState = null;

        while(System.currentTimeMillis() < endTime && !instanceState.equals(currentInstanceState))
        {
            TimeUnit.MILLISECONDS.sleep(loopWaitTimeMS);
            List<Reservation> reservations = ec2Client.describeInstances().getReservations();
            for(Reservation r : reservations){
                for(Instance i : r.getInstances()){
                    if(instanceID.equals(i.getInstanceId()))
                    {
                        currentInstanceState = i.getState().getName();
                    }
                }
            }
        }

        if(instanceState.equals(currentInstanceState)){
            System.out.println("Instance (ID: " + instanceID + ") is now in Status '" + instanceState + "'");
        }
        else {
            System.out.println("Status '" + instanceState + "' could not be reached. Instance (ID: " + instanceID + ") is now in Status '" + currentInstanceState + "'...");
        }
        System.out.println("Waited for " + ((System.currentTimeMillis() - startTime) / 1000)  + " seconds...");
    }

    public static HashMap<String, String> getPublicIPandDNS(AmazonEC2Client ec2Client, String instanceID){

        System.out.println("Retrieving public IP and DNS for Instance (ID:" + instanceID + ")...");
        String publicDNS = "";
        String publicIP = "";
        String  privateIP = "";

        List<Reservation> reservations = ec2Client.describeInstances().getReservations();
        for(Reservation r : reservations){
            for(Instance i : r.getInstances()){
                if(instanceID.equals(i.getInstanceId()))
                {
                    publicDNS = r.getInstances().get(0).getPublicDnsName();
                    publicIP = r.getInstances().get(0).getPublicIpAddress();
                    privateIP = r.getInstances().get(0).getPrivateIpAddress();
                }
            }
        }

        System.out.println("Instance IP/DNS (" + instanceID + ")");
        System.out.println(" - Public DNS: " + publicDNS);
        System.out.println(" - Public IP: " + publicIP);
        System.out.println(" - Private IP: " + privateIP);

        HashMap<String, String> dnsIP = new HashMap<String, String>();

        dnsIP.put("IP", publicIP);
        dnsIP.put("DNS", publicDNS);
        dnsIP.put("ip", publicIP);
        dnsIP.put("dns", publicDNS);
        dnsIP.put("privIP", privateIP);

        return dnsIP;
    }

    public static Set<Instance> describeAllInstances(AmazonEC2Client ec2Client){
        List<Reservation> reservations = ec2Client.describeInstances().getReservations();
        Set<Instance> instances = new HashSet<Instance>();
        // add all instances to a Set.
        for (Reservation reservation : reservations) {
            instances.addAll(reservation.getInstances());
        }

        System.out.println("You have " + instances.size() + " Amazon EC2 instance(s).");
        for (Instance ins : instances) {

            // instance id
            String instanceId = ins.getInstanceId();

            // instance state
            InstanceState is = ins.getState();
            System.out.println(instanceId + " " + is.getName());
        }

        return instances;
    }
}
