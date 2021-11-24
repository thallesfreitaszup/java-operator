package sdk.operator.handler;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.cache.Cache;
import io.fabric8.kubernetes.client.informers.cache.Lister;
import sdk.operator.controller.CharlesDeploymentController;
import sdk.operator.resource.charlesdeployment.CharlesDeployment;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class ServiceEventHandler implements ResourceEventHandler<Service> {
    public static final Logger logger = Logger.getLogger(CharlesDeploymentController.class.getName());
    private final BlockingQueue<String> workqueue;
    private final Lister<CharlesDeployment> charlesDeploymentLister;

    public ServiceEventHandler(BlockingQueue<String> workqueue, Lister<CharlesDeployment> charlesDeploymentLister) {
        this.workqueue = workqueue;
        this.charlesDeploymentLister = charlesDeploymentLister;
    }

    @Override
    public void onAdd(Service service) {

    }

    @Override
    public void onUpdate(Service service, Service t1) {

    }

    @Override
    public void onDelete(Service service, boolean b) {
        logger.info("Service " + service.getMetadata().getName() + " ADDED");
        var ownerRef = service.getMetadata().getOwnerReferences().stream().filter(it -> it.getKind().equals("CharlesDeployment")).findAny();
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


