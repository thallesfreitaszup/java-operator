package sdk.operator.handler;

import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;

import java.util.concurrent.BlockingQueue;

public class DeploymentEventHandler implements ResourceEventHandler<Deployment> {

    private final BlockingQueue<String> workqueue;

    public DeploymentEventHandler(BlockingQueue<String> workqueue) {
        this.workqueue = workqueue;
    }

    @Override
    public void onAdd(Deployment deployment) {

    }

    @Override
    public void onUpdate(Deployment deployment, Deployment t1) {

    }

    @Override
    public void onDelete(Deployment deployment, boolean b) {

    }
}


