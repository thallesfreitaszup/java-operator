package sdk.operator.template;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.util.Yaml;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesObject;
import org.yaml.snakeyaml.constructor.Constructor;
import sdk.operator.file.FileUtils;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Helm {
    private final String tgzPath;
    private final Path tmpPath;

    public Helm() throws IOException {
        this.tmpPath = Files.createTempDirectory("helm-");
        this.tgzPath = String.format("%s/%s", tmpPath.toAbsolutePath().toString(), "component.tgz");
    }

    public void DownloadTGZ(String url) {
        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(tgzPath)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to download tgz: " + e.getMessage());
        }
    }

    public List<DynamicKubernetesObject> template(String downloadUrl, String componentName) throws IOException, InterruptedException {
        this.DownloadTGZ(downloadUrl);
        this.saveChartFiles();
        var manifestsString = this.exec(String.format("helm template %s/%s --namespace=default", tmpPath.toAbsolutePath(), componentName));
        var manifests = getManifestsObjects(manifestsString);
        this.cleanFiles();
        return manifests;
    }

    private List<DynamicKubernetesObject> getManifestsObjects(String manifestsString) throws IOException {
        List<Object> manifestObjects =  Yaml.loadAll(manifestsString);
        var manifests = manifestObjects.stream()
                .map(object -> (KubernetesObject) object)
                .map(manifestObject -> new GsonBuilder().setLenient().create().toJson(manifestObject))
                .map(manifestString -> JsonParser.parseString(manifestString).getAsJsonObject())
                .map(DynamicKubernetesObject::new)
                .collect(Collectors.toList());
        System.out.println(manifests);
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
