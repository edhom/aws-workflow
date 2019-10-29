
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.PushResponseItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DockerUtils {

    public static DockerClient loadDockerDefaultConfig(){
        return loadDockerConfig("");
    }
    public static DockerClient loadDockerConfig(String url){
        InputStream configFile = null;
        String DOCKER_HOST="";
        String username="";
        String password="";
        String version="";

        try
        {
            // load a awsconfig file
            configFile = new FileInputStream("dockerconfig");

            if (configFile == null) {
                System.out.println("Unable to find dockerconfig file. Exiting program...");
                System.exit(1);
            }

            Properties prop = new Properties();

            prop.load(configFile);

            //get id and secret
            DOCKER_HOST=prop.getProperty("DOCKER_HOST");
            if(url == null || url.equals("")) {
                url = prop.getProperty("url");
            }
            version=prop.getProperty("version");
            username=prop.getProperty("username");
            password=prop.getProperty("password");

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(DOCKER_HOST)
                .withRegistryUrl(url)
                .withRegistryUsername(username)
                .withRegistryPassword(password)
                .build();

        DockerCmdExecFactory dockerCmdExecFactory = new JerseyDockerCmdExecFactory()
                .withReadTimeout(10000)
                .withConnectTimeout(10000)
                .withMaxTotalConnections(100)
                .withMaxPerRouteConnections(10);

        DockerClient dockerClient = DockerClientBuilder.getInstance(config)
                .withDockerCmdExecFactory(dockerCmdExecFactory)
                .build();

        return dockerClient;
    }

    public static String getFullImgTag(DockerClient dockerClient, String repository, String imgTag){
        return dockerClient.authConfig().getUsername() + "/" + repository + ":" + imgTag;
    }

    public static void buildImgFromDockerfile(DockerClient dockerClient, String dockerFilePath, String fullImageTag) throws IOException{

        File relativePath = new File(dockerFilePath);
        String absolutePath = relativePath.getCanonicalPath();

        System.out.println("Building docker image with dockerfile: " + absolutePath);

        File baseDir = new File(absolutePath);

        System.out.println(absolutePath);
        BuildImageResultCallback callback = new BuildImageResultCallback() {
            @Override
            public void onNext(BuildResponseItem item) {
                System.out.println("" + item);
                super.onNext(item);
            }
        };

        String imageID = dockerClient.buildImageCmd(baseDir).withTag(fullImageTag).exec(callback).awaitImageId();

        System.out.println("Build successfull...");

        System.out.println("Docker ImageID: " + imageID);
    }

    public static PushImageResultCallback pushImgToDockerHub(DockerClient dockerClient, String repository, String imgTag) throws InterruptedException{
        PushImageResultCallback callback = new PushImageResultCallback() {
            @Override
            public void onNext(PushResponseItem item) {
                System.out.println("" + item);
                super.onNext(item);
            }
        };

        System.out.println("pushed to hub");
        return dockerClient.pushImageCmd(dockerClient.authConfig().getUsername() + "/" + repository + ":" + imgTag)
                .withAuthConfig(dockerClient.authConfig())
                .exec(callback)
                .awaitCompletion();


    }

    public static PullImageResultCallback pullImgFromDockerHub(DockerClient dockerClient, String repository, String imageTag) throws InterruptedException{
        PullImageResultCallback callback = new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem item) {
                System.out.println("" + item);
                super.onNext(item);
            }
        };
        System.out.println("pull from hub");
        return dockerClient.pullImageCmd(dockerClient.authConfig().getUsername() + "/" + repository + ":" + imageTag)
                .withAuthConfig(dockerClient.authConfig())
                .exec(callback)
                .awaitCompletion();
    }
}
