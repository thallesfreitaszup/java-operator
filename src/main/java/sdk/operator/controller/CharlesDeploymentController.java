package sdk.operator.controller;

import sdk.operator.integrations.repository.Factory;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.cache.Lister;
import sdk.operator.handler.CharlesDeploymentEventHandler;
import sdk.operator.handler.DeploymentEventHandler;
import sdk.operator.handler.ServiceEventHandler;
import sdk.operator.resource.charlesdeployment.CharlesDeployment;
import sdk.operator.resource.charlesdeployment.CharlesDeploymentList;
import sdk.operator.resource.charlesdeployment.ChildResource;
import sdk.operator.resource.component.Component;
import sdk.operator.template.Helm;
import sdk.operator.utils.K8sUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    private final MixedOperation<CharlesDeployment, CharlesDeploymentList, Resource<CharlesDeployment>> charlesDeploymentClient;
    private final Helm helm;
    private final K8sUtils k8sUtils;

    public CharlesDeploymentController(
            SharedIndexInformer<Deployment> deploymentInformer,
            SharedIndexInformer<CharlesDeployment> charlesDeploymentSharedIndexInformer,
            SharedIndexInformer<Service> serviceInformer,
            KubernetesClient kubernetesClient,
            MixedOperation<CharlesDeployment, CharlesDeploymentList, Resource<CharlesDeployment>> charlesDeploymentClient
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
        this.charlesDeploymentClient = charlesDeploymentClient;
    }
    public void create() {
        ResourceEventHandler<Deployment> deploymentEventHandler = new DeploymentEventHandler(this.workqueue, this.charlesDeploymentLister);
        ResourceEventHandler<Service> serviceEventHandler = new ServiceEventHandler(this.workqueue, this.charlesDeploymentLister);
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

        var notSyncedComponents = getNotSyncComponents(charlesDeployment);
        if (notSyncedComponents.isEmpty()) {
            return;
        }
         notSyncedComponents.stream().parallel().forEach(
                 it -> this.createCharlesComponent(it, charlesDeployment)
         );
    }

    private boolean isChildSync(ChildResource it) {
        if (this.k8sUtils.getResource(it.getApiVersion(), it.getPlural(), it.getName()) == null ) {
            return false;
        }
        return true;
    }

    private List<Component> getNotSyncComponents(CharlesDeployment charlesDeployment) {
        return charlesDeployment.getSpec().getComponents()
                .stream()
                .filter(it -> isNotSync(it, charlesDeployment.getSpec().getChildResources()))
                .collect(Collectors.toList());
    }

    private boolean isNotSync(Component component, List<ChildResource> childResourceList) {
        if ( childResourceList == null || childResourceList.isEmpty()){
            return true;
        }
        
        var unSyncChildren = childResourceList.stream().filter(it -> it.getComponentName().equals(component.getName())).filter(
                childResource -> !isChildSync(childResource)
        ).collect(Collectors.toList());
        return !unSyncChildren.isEmpty();
    }

    private void createCharlesComponent(Component component, CharlesDeployment charlesDeployment) {
        try {
            var repository = Factory.newRepository(component.getProvider());
            var contents = repository.getContent(component);
            var tgzContent = repository.getTGZFromContent(contents);
            var manifests = this.helm.template(tgzContent.getDownloadUrl(), component);
            for (var manifest : manifests) {
                this.k8sUtils.applyManifest(manifest, charlesDeployment);
                charlesDeployment.createChildren(component.getName(),manifest);
            }
            charlesDeployment.updateStatus(true);
            updateCharlesDeploymentStatus(charlesDeployment);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCharlesDeploymentStatus(CharlesDeployment charlesDeployment) {
        this.charlesDeploymentClient.inNamespace(charlesDeployment.getMetadata().getNamespace())
                .withName(charlesDeployment.getMetadata().getName()).patch(charlesDeployment);
    }

}
