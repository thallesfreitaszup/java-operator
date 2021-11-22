package sdk.operator.handler;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import sdk.operator.controller.CharlesDeploymentController;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class DeploymentEventHandler implements ResourceEventHandler<Deployment> {

    private final BlockingQueue<String> workqueue;
    public static final Logger logger = Logger.getLogger(CharlesDeploymentController.class.getName());
    public DeploymentEventHandler(BlockingQueue<String> workqueue) {
        this.workqueue = workqueue;
    }

    @Override
    public void onAdd(Deployment deployment) {
        logger.info("Deployment " + deployment.getMetadata().getName() + " ADDED");
    }

    @Override
    public void onUpdate(Deployment deployment, Deployment t1) {
        logger.info("Deployment " + deployment.getMetadata().getName() + " ADDED");
    }

    @Override
    public void onDelete(Deployment deployment, boolean b) {
        logger.info("Deployment " + deployment.getMetadata().getName() + " ADDED");
    }
}


