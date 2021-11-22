package sdk.operator.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Github implements Repository {

    public static final Logger logger = Logger.getLogger(Github.class.getName());
    public static final ObjectMapper mapper = new ObjectMapper();
    @Override
    public List<Content> getContent(String url) {
        logger.log(Level.INFO, "START_GETTING_REPO_CONTENT",url);
       try {
           var response = makeRequest(url);
           if (response.isEmpty()) {
               throw new RuntimeException("No response from git request");
           }
            Content[] resources = mapper.readValue(response.get(), Content[].class);
           response.get().close();
           logger.log(Level.INFO, "FINISH_GETTING_REPO_CONTENT",url);
            return  Arrays.asList(resources);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public Content getTGZFromContent(List<Content> resources) {
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
