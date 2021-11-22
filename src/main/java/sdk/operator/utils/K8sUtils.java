package sdk.operator.utils;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.ListOptions;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.informers.cache.Lister;
import sdk.operator.resource.charlesdeployment.CharlesDeployment;

import java.util.List;
import java.util.Locale;

public class K8sUtils {
    private final KubernetesClient client;

    public K8sUtils(KubernetesClient kubernetesClient) {
        this.client = kubernetesClient;
    }

    public void applyManifest(GenericKubernetesResource manifest, CharlesDeployment charlesDeployment) {
        var context  = this.getManifestContext(manifest);
        createOwnerReference(manifest, charlesDeployment);
        var output = client.genericKubernetesResources(context).inNamespace("default").create(manifest);
        this.waitUntilHealthy(manifest, context);
    }

    private void createOwnerReference(GenericKubernetesResource manifest, CharlesDeployment charlesDeployment) {
        var ownerRef = new OwnerReferenceBuilder().withKind(charlesDeployment.getKind()).withName(
                charlesDeployment.getMetadata().getName()
        ).withController(true).withApiVersion(charlesDeployment.getApiVersion()).withUid(charlesDeployment.getMetadata().getUid()).build();
     manifest.getMetadata().setOwnerReferences(List.of(ownerRef));
    }

    private CustomResourceDefinitionContext getManifestContext(GenericKubernetesResource manifest) {
        var group = "";
        var  version = "";
        var arrGroupVersion = manifest.getApiVersion().split("/");
        if (arrGroupVersion.length == 2) {
            group = arrGroupVersion[0];
            version = arrGroupVersion[1];
        } else {
            version = arrGroupVersion[0];
        }
        var context = new CustomResourceDefinitionContext.Builder()
                .withGroup(group)
                .withVersion(version)
                .withScope("Namespaced")
                .withPlural(manifest.getKind().toLowerCase(Locale.ROOT)+"s")
                .withKind(manifest.getKind())
                .build();
        return context;
    }

    public void waitUntilHealthy(GenericKubernetesResource genericKubernetesResource, CustomResourceDefinitionContext context) {
        var createdDeployment = client.genericKubernetesResources(context).withName(genericKubernetesResource.getMetadata().getName());
        
    }
}
