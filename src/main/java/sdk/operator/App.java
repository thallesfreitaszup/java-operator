package sdk.operator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.fabric8.kubernetes.client.Client;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesApi;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesObject;
import sdk.operator.repository.Factory;
import sdk.operator.repository.RepositoryType;
import sdk.operator.template.Helm;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException, InterruptedException, ApiException {
        var helm = new Helm();
        var repository = Factory.newRepository(RepositoryType.GITHUB);
        var content = repository.getContent("https://api.github.com/repos/thallesfreitaszup/event-receiver/contents/event-receiver?ref=main");
        var tgzContent = repository.getTGZFromContent(content);
        var k8sObjects = helm.template(tgzContent.getDownloadUrl(), "event-receiver");
        ApiClient apiClient = ClientBuilder.standard().build();
        for (var k8sObject : k8sObjects){
            var plural = String.format("%s%s", k8sObject.getKind().toLowerCase(Locale.ROOT), "s");
            DynamicKubernetesApi dynamicApi = new DynamicKubernetesApi("", k8sObject.getApiVersion(), plural, apiClient);
            var createdObject = dynamicApi.create(k8sObject).throwsApiException().getHttpStatusCode();
            System.out.println(createdObject);
        }
    }
}
