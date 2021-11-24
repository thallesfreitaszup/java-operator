package sdk.operator.handler;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.cache.Cache;
import io.fabric8.kubernetes.client.informers.cache.Lister;
import sdk.operator.controller.CharlesDeploymentController;
import sdk.operator.resource.charlesdeployment.CharlesDeployment;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class DeploymentEventHandler implements ResourceEventHandler<Deployment> {

    private final BlockingQueue<String> workqueue;
    public static final Logger logger = Logger.getLogger(CharlesDeploymentController.class.getName());
    private final Lister<CharlesDeployment> charlesDeploymentLister;

    public DeploymentEventHandler(BlockingQueue<String> workqueue, Lister<CharlesDeployment> charlesDeploymentLister) {
        this.workqueue = workqueue;
        this.charlesDeploymentLister = charlesDeploymentLister;
    }

    @Override
    public void onAdd(Deployment deployment) {
        logger.info("Deployment " + deployment.getMetadata().getName() + " ADDED");
        var ownerRef = deployment.getMetadata().getOwnerReferences().stream().filter(it -> it.getKind().equals("CharlesDeployment")).findAny();
        if (ownerRef.isPresent()) {
            var charlesDeployment = this.charlesDeploymentLister.get(ownerRef.get().getName());
            var key = Cache.metaNamespaceKeyFunc(charlesDeployment);
            if ( key != null && !key.isEmpty()){
                logger.info(String.format("Queueing key %s of resource %s", key, charlesDeployment.getMetadata().getName()));
                this.workqueue.add(key);
            }
        }
    }

    @Override
    public void onUpdate(Deployment deployment, Deployment t1) {
        logger.info("Deployment " + deployment.getMetadata().getName() + " UPDATED");
    }

    @Override
    public void onDelete(Deployment deployment, boolean b) {
        logger.info("Deployment " + deployment.getMetadata().getName() + " DELETED");
        var ownerRef = deployment.getMetadata().getOwnerReferences().stream().filter(it -> it.getKind().equals("CharlesDeployment")).findAny();
        if (ownerRef.isPresent()) {
            var charlesDeployment = this.charlesDeploymentLister.get(ownerRef.get().getName());
            var key = Cache.metaNamespaceKeyFunc(charlesDeployment);
            if ( key != null && !key.isEmpty()){
                logger.info(String.format("Queueing key %s of resource %s", key, charlesDeployment.getMetadata().getName()));
                this.workqueue.add(key);
            }
        }
    }
}


