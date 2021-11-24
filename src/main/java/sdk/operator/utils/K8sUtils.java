package sdk.operator.utils;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;

import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import sdk.operator.controller.CharlesDeploymentController;
import sdk.operator.resource.charlesdeployment.CharlesDeployment;

import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

public class K8sUtils {
    public static final Logger logger = Logger.getLogger(K8sUtils.class.getName());
    private final KubernetesClient client;

    public K8sUtils(KubernetesClient kubernetesClient) {
        this.client = kubernetesClient;
    }

    public void applyManifest(GenericKubernetesResource manifest, CharlesDeployment charlesDeployment) {
        if (resourceExists(manifest)) {
            logger.info(String.format("Resource %s already exists.Skipping...",manifest.getMetadata().getName()));
            return;
        }
        var context  = this.getManifestContext(manifest);
        createOwnerReference(manifest, charlesDeployment);
        var output = client.genericKubernetesResources(context).inNamespace("default").createOrReplace(manifest);
        logger.info(String.format("Resource %s/%s applied.",manifest.getApiVersion(),manifest.getMetadata().getName()));
    }

    private boolean resourceExists(GenericKubernetesResource manifest) {
        var resource = this.getResource(
                manifest.getApiVersion(),
                manifest.getKind().toLowerCase(Locale.ROOT)+"s",
                manifest.getMetadata().getName());
        if (resource != null) {
            return true;
        }
        return false;
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


    private CustomResourceDefinitionContext getManifestContext(String apiVersion, String plural) {
        var group = "";
        var  version = "";
        var arrGroupVersion = apiVersion.split("/");
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
                .withPlural(plural)
                .build();
        return context;
    }

    public GenericKubernetesResource getResource(String apiVersion, String plural, String name) {
        var context = this.getManifestContext(apiVersion, plural);
        var resource = client.genericKubernetesResources(context).inNamespace("default").withName(name).get();
        return resource;
    }
}
