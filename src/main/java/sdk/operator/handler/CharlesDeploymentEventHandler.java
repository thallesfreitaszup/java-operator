package sdk.operator.handler;

import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.cache.Cache;
import sdk.operator.controller.DeploymentController;
import sdk.operator.resource.charlesdeployment.CharlesDeployment;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class CharlesDeploymentEventHandler implements ResourceEventHandler<CharlesDeployment> {

    private final BlockingQueue<String> workqueue;

    public static final Logger logger = Logger.getLogger(DeploymentController.class.getName());

    public CharlesDeploymentEventHandler(BlockingQueue<String> workqueue) {
        this.workqueue = workqueue;
    }

    @Override
    public void onAdd(CharlesDeployment charlesDeployment) {
        logger.info("CharlesDeployment " + charlesDeployment.getMetadata().getName() + " ADDED");
            enqueueCharlesDeployment(charlesDeployment);
    }


    @Override
    public void onUpdate(CharlesDeployment charlesDeployment, CharlesDeployment t1) {

    }

    @Override
    public void onDelete(CharlesDeployment charlesDeployment, boolean b) {
    }


    private void enqueueCharlesDeployment(CharlesDeployment charlesDeployment) {
        String key = Cache.metaNamespaceKeyFunc(charlesDeployment);
        if (key != null && !key.isEmpty()) {
            logger.info(String.format("Enqueueing key %s", key));
            workqueue.add(key);
        }
    }

}


