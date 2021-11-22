package sdk.operator.controller;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.cache.Lister;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesApi;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesObject;
import sdk.operator.handler.CharlesDeploymentEventHandler;
import sdk.operator.handler.DeploymentEventHandler;
import sdk.operator.handler.ServiceEventHandler;
import sdk.operator.repository.Factory;
import sdk.operator.resource.charlesdeployment.CharlesDeployment;
import sdk.operator.resource.component.Component;
import sdk.operator.template.Helm;
import sdk.operator.utils.K8sUtils;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CharlesDeploymentController {

    private final BlockingQueue<String> workqueue;
    private final SharedIndexInformer<CharlesDeployment> charlesDeploymentInformer;
    private final SharedIndexInformer<Deployment> deploymentInformer;
    private final Lister<CharlesDeployment> charlesDeploymentLister;
    private final Lister<Deployment> deploymentLister;
    private final Lister<Service> serviceLister;
    private final SharedIndexInformer<Service> serviceInformer;
    private final KubernetesClient kubernetesClient;
    public static final Logger logger = Logger.getLogger(CharlesDeploymentController.class.getName());
    private  Helm helm;
    private  K8sUtils k8sUtils;

    public CharlesDeploymentController(
            SharedIndexInformer<Deployment> deploymentInformer,
            SharedIndexInformer<CharlesDeployment> charlesDeploymentSharedIndexInformer,
            SharedIndexInformer<Service> serviceInformer,
            KubernetesClient kubernetesClient
            ) {
        this.workqueue = new ArrayBlockingQueue<>(1024);
        this.deploymentInformer = deploymentInformer;
        this.charlesDeploymentInformer = charlesDeploymentSharedIndexInformer;
        this.charlesDeploymentLister= new Lister<CharlesDeployment>(charlesDeploymentSharedIndexInformer.getIndexer(), "default");
        this.deploymentLister= new Lister<Deployment>(deploymentInformer.getIndexer(), "default");
        this.serviceLister = new Lister<Service>(serviceInformer.getIndexer(), "default");
        this.serviceInformer = serviceInformer;
        this.kubernetesClient = kubernetesClient;
        this.helm = new Helm();
        this.k8sUtils = new K8sUtils(this.kubernetesClient);
    }
    public void create() {
        ResourceEventHandler<Deployment> deploymentEventHandler = new DeploymentEventHandler(this.workqueue);
        ResourceEventHandler<Service> serviceEventHandler = new ServiceEventHandler(this.workqueue);
        ResourceEventHandler<CharlesDeployment> charlesDeploymentEventHandler = new CharlesDeploymentEventHandler(this.workqueue);

        this.deploymentInformer.addEventHandler(deploymentEventHandler);
        this.serviceInformer.addEventHandler(serviceEventHandler);
        this.charlesDeploymentInformer.addEventHandler(charlesDeploymentEventHandler);
    }

    public void run() {
        logger.log(Level.INFO, "Starting CharlesDeployment controller");
        while (!charlesDeploymentInformer.hasSynced() || !deploymentInformer.hasSynced() || !serviceInformer.hasSynced()) {
            // Wait till Informer syncs
        }

        while (true) {
            try {
                logger.log(Level.INFO, "trying to fetch item from workqueue...");
                if (workqueue.isEmpty()) {
                    logger.log(Level.INFO, "Work Queue is empty");
                }
                String key = workqueue.take();
                Objects.requireNonNull(key, "key can't be null");
                logger.log(Level.INFO, String.format("Got %s", key));
                if ((!key.contains("/"))) {
                    logger.log(Level.WARNING, String.format("invalid resource key: %s", key));
                }

                // Get the CharlesDeployment resource's name from key which is in format namespace/name
                String name = key.split("/")[1];
                CharlesDeployment charlesDeployment = charlesDeploymentLister.get(key.split("/")[1]);
                if (charlesDeployment == null) {
                    logger.log(Level.SEVERE, String.format("CharlesDeployment %s in workqueue no longer exists", name));
                    return;
                }
                reconcile(charlesDeployment);

            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                logger.log(Level.SEVERE, "controller interrupted..");
            }
        }
    }

    private void reconcile(CharlesDeployment charlesDeployment) {
             charlesDeployment.getSpec().getComponents().stream().parallel().forEach(
                     it -> this.createCharlesComponent(it, charlesDeployment)
             );
    }

    private void createCharlesComponent(Component component, CharlesDeployment charlesDeployment) {
        try {
            var repository = Factory.newRepository(component.getProvider());
            var contents = repository.getContent(component.getChart());
            var tgzContent = repository.getTGZFromContent(contents);
            var manifests = this.helm.template(tgzContent.getDownloadUrl(), component.getName(), component.getNamespace());
            for (var manifest : manifests) {
                    this.k8sUtils.applyManifest(manifest, charlesDeployment);
            }
//            DynamicKubernetesApi = new DynamicKubernetesApi("", "")
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
