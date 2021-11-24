package sdk.operator.template;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.client.utils.Serialization;
import sdk.operator.file.FileUtils;
import sdk.operator.integrations.repository.Github;
import sdk.operator.resource.component.Component;

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
    private String tgzPath;
    private Path tmpPath;
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
        if ( !Files.exists(this.tmpPath))
        {
            this.tmpPath = createTmpDir();
            this.tgzPath = String.format("%s/%s", tmpPath.toAbsolutePath().toString(), "component.tgz");
        }
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

    public List<GenericKubernetesResource> template(String downloadUrl, Component component) throws IOException, InterruptedException {
        this.logger.info("START_GENERATE_TEMPLATE"+downloadUrl);
        this.DownloadTGZ(downloadUrl);
        this.saveChartFiles();
        var overrideValues = getValuesToOverride(component.getImage());
        var valuesPath = String.format("%s/%s/%s.yaml", tmpPath.toAbsolutePath(), component.getName(), component.getName());
        var command = String.format(
                "helm template  %s/%s --namespace=%s  --values=%s  --name-template=%s --set %s",
                tmpPath.toAbsolutePath(), component.getName(), component.getNamespace(),valuesPath, component.getName(), overrideValues);
        var manifestsString = this.exec(command);
        var manifests = getManifestsObjects(manifestsString);
        this.cleanFiles();
        manifests.removeAll(Collections.singleton(null));
        this.logger.log(Level.INFO, "FINISH_GENERATE_TEMPLATE", manifests);
        return manifests;
    }

    private String getValuesToOverride(String image) {
        return String.format("image.url=%s",image);
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

    private String exec(String cmd)  {
        try {
            Process process = Runtime.getRuntime().exec(cmd);


            StringBuilder output = new StringBuilder();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

            int exitVal = process.waitFor();
            if (exitVal != 0) {
                throw new RuntimeException("Exited with error code: "+exitVal);
            }
        return output.toString();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Exited with error code: "+e.getMessage());
        }
    }

}
