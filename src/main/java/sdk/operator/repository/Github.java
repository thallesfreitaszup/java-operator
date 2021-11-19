package sdk.operator.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import sdk.operator.configuration.AppConstants;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

import static sdk.operator.configuration.AppConstants.HELM_REPOSITORY_PATH;


public class Github implements Repository {

    public static final Logger logger = Logger.getLogger(Github.class.getName());
    public static final ObjectMapper mapper = new ObjectMapper();
    @Override
    public List<Resource> getContent(String url) {
       try {
           var response = makeRequest(url);
           if (response.isEmpty()) {
               throw new RuntimeException("No response from git request");
           }
            Resource[] resources = mapper.readValue(response.get(), Resource[].class);
           response.get().close();
            return  Arrays.asList(resources);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<Resource> GetValuesFromContent(List<Resource> resources, String componentName) {
        return resources.stream().filter(it -> it.getName().contains(componentName)).findAny();
    }

    @Override
    public Resource getTGZFromContent(List<Resource> resources) {
        var tgzResource = resources.stream().filter(it -> it.getName().contains(".tgz")).findAny();
        if (tgzResource.isEmpty()) {
            throw new RuntimeException("No tgz found");
        }
        return tgzResource.get();
    }


    private Optional<InputStream> makeRequest(String url){

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build(); // defaults to GET

        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException("Error downloading resource: "+ e.getMessage());
        }

        if (response.body() != null){
            return Optional.of(response.body().byteStream());
        }
        return Optional.empty();
    }
}
