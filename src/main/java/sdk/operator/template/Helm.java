package sdk.operator.template;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.client.utils.Serialization;
import sdk.operator.file.FileUtils;
import sdk.operator.repository.Github;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Helm {
    private final String tgzPath;
    private final Path tmpPath;
    public static final Logger logger = Logger.getLogger(Github.class.getName());
    public Helm()  {
        this.tmpPath = createTmpDir();
        this.tgzPath = String.format("%s/%s", tmpPath.toAbsolutePath().toString(), "component.tgz");
    }

    private Path createTmpDir() {
        Path tmpDir = null;
        try {
           tmpDir =  Files.createTempDirectory("helm-");
        }catch (IOException e ){
            throw new RuntimeException("Error creating tempfile "+ e.getMessage());
        }
        return tmpDir;
    }

    public void DownloadTGZ(String url) {
        logger.log(Level.INFO, "START_DOWNLOAD_TGZ",url);
        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(tgzPath)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            logger.log(Level.INFO, "FINISHED_DOWNLOAD_TGZ",url);
        } catch (IOException e) {
            throw new RuntimeException("Failed to download tgz: " + e.getMessage());
        }
    }

    public List<GenericKubernetesResource> template(String downloadUrl, String componentName, String namespace) throws IOException, InterruptedException {
        this.logger.info("START_GENERATE_TEMPLATE");
        this.DownloadTGZ(downloadUrl);
        this.saveChartFiles();
        var manifestsString = this.exec(String.format("helm template %s/%s --namespace=%s", tmpPath.toAbsolutePath(), componentName, namespace));
        var manifests = getManifestsObjects(manifestsString);
        this.cleanFiles();
        manifests.removeAll(Collections.singleton(null));
        this.logger.info("FINISH_GENERATE_TEMPLATE");
        return manifests;
    }

    private List<GenericKubernetesResource> getManifestsObjects(String manifestsString) throws IOException {
        List<String> manifestsArray = List.of(manifestsString.split("---"));
        var manifests = manifestsArray.stream()
                .map( string -> Serialization.unmarshal(string, GenericKubernetesResource.class))
                .collect(Collectors.toList());
        return manifests;
    }

    private void cleanFiles() throws IOException {
        FileUtils.removeRecursive(tmpPath);
    }

    private void saveChartFiles() throws IOException {
        FileUtils.decompress(tgzPath, new File(tmpPath.toAbsolutePath().toString()));
    }

    private String exec(String cmd) throws InterruptedException, IOException {
        StringBuffer singleFile = new StringBuffer();
        Runtime run = Runtime.getRuntime();
        Process pr = run.exec(cmd);
        pr.waitFor();
        var bufReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        var line = "";
        while((line = bufReader.readLine()) != null){
            singleFile.append(line);
            singleFile.append(System.getProperty("line.separator"));
        }
        return singleFile.toString();
    }

}
