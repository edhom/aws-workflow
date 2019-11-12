package Homework;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.connection.channel.direct.Session.Command;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class SSHUtils {

    public static SSHClient connectSSH(String publicDNS, String user, String keyName) throws IOException {
        SSHClient ssh = new SSHClient();
        //faster transfer with compression
        ssh.useCompression();
        //disable verifying fingerprint off SSH
        ssh.addHostKeyVerifier(new PromiscuousVerifier());

        //connect session
        System.out.println("Connecting via SSH to '" + publicDNS + "' with user '" + user + "' and key file '" + keyName + "'");

        if (System.getProperty("os.name").startsWith("Windows")) {
            try {
                ssh.connect(publicDNS);
            } catch (Exception e) {

                System.out.println("Connection failed - trying again...");
                e.printStackTrace();
            }
        } else { // On Unix, continue trying
            boolean connectionSuccess = false;
            while(!connectionSuccess){
                connectionSuccess = true;
                try{
                    ssh.connect(publicDNS);
                }
                catch (Exception e){
                    connectionSuccess = false;
                    System.out.println("Connection failed - trying again...");
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Connection successful...");

        //authenticate with private key file
        ssh.authPublickey(user, keyName);

        return ssh;
    }

    public static void executeCMD(SSHClient sshClient, String command, int timeOutSeconds) throws IOException{
            System.out.println("SSH Exec: " + command);
            Session session = null;
            session = sshClient.startSession();
            final Command cmd = session.exec(command);
            System.out.print(IOUtils.readFully(cmd.getInputStream()).toString());
            cmd.join(timeOutSeconds, TimeUnit.SECONDS);
            System.out.print("\n** exit status: " + cmd.getExitStatus() + "\n");
    }

    public static void SCPDownload(SSHClient sshClient, String remoteFilePath, String localPath) throws IOException {
        System.out.println("Downloading file '" + sshClient.getRemoteHostname() + ":" + remoteFilePath + "' to '"   + localPath + "'");
        //download file from remote destination path
        sshClient.newSCPFileTransfer().download(remoteFilePath, new FileSystemFile(localPath));
    }

    public static void SCPUpload(SSHClient sshClient, String file, String remotePath) throws IOException {
        System.out.println("Uploading file '" + file + "' to '" + sshClient.getRemoteHostname() + ":" + remotePath);
        //upload file to remote destination path
        sshClient.newSCPFileTransfer().upload(new FileSystemFile(file), remotePath);
    }
}