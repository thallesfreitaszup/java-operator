package sdk.operator.controller;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.cache.Lister;
import sdk.operator.handler.CharlesDeploymentEventHandler;
import sdk.operator.handler.DeploymentEventHandler;
import sdk.operator.handler.ServiceEventHandler;
import sdk.operator.repository.Factory;
import sdk.operator.resource.charlesdeployment.CharlesDeployment;
import sdk.operator.resource.component.Component;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeploymentController  {

    private final BlockingQueue<String> workqueue;
    private final SharedIndexInformer<CharlesDeployment> charlesDeploymentInformer;
    private final SharedIndexInformer<Deployment> deploymentInformer;
    private final Lister<CharlesDeployment> charlesDeploymentLister;
    private final Lister<Deployment> deploymentLister;
    private final Lister<Service> serviceLister;
    private final SharedIndexInformer<Service> serviceInformer;
    private final KubernetesClient kubernetesClient;
    public static final Logger logger = Logger.getLogger(DeploymentController.class.getName());

    public DeploymentController(
            SharedIndexInformer<Deployment> deploymentInformer,
            SharedIndexInformer<CharlesDeployment> charlesDeploymentSharedIndexInformer,
            Lister<CharlesDeployment> charlesDeploymentLister,
            Lister<Deployment> deploymentLister,
            Lister<Service> serviceLister,
            SharedIndexInformer<Service> serviceInformer,
            KubernetesClient kubernetesClient
            ) {
        this.workqueue = new ArrayBlockingQueue<>(1024);
        this.deploymentInformer = deploymentInformer;
        this.charlesDeploymentInformer = charlesDeploymentSharedIndexInformer;
        this.charlesDeploymentLister= charlesDeploymentLister;
        this.deploymentLister= deploymentLister;
        this.serviceLister = serviceLister;
        this.serviceInformer = serviceInformer;
        this.kubernetesClient = kubernetesClient;
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
        logger.log(Level.INFO, "Starting PodSet controller");
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

                // Get the PodSet resource's name from key which is in format namespace/name
                String name = key.split("/")[1];
                CharlesDeployment charlesDeployment = charlesDeploymentLister.get(key.split("/")[1]);
                if (charlesDeployment == null) {
                    logger.log(Level.SEVERE, String.format("PodSet %s in workqueue no longer exists", name));
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
               component -> createCharlesComponent(component)
        );
    }

    private void createCharlesComponent(Component component) {
        var repository = Factory.newRepository(component.getProvider());
        repository.getContent(component.getChart());

    }

}
