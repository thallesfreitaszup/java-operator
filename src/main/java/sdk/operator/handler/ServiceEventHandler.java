package sdk.operator.handler;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;

import java.util.concurrent.BlockingQueue;

public class ServiceEventHandler implements ResourceEventHandler<Service> {


    private final BlockingQueue<String> workqueue;

    public ServiceEventHandler(BlockingQueue<String> workqueue) {
        this.workqueue = workqueue;
    }

    @Override
    public void onAdd(Service service) {

    }

    @Override
    public void onUpdate(Service service, Service t1) {

    }

    @Override
    public void onDelete(Service service, boolean b) {

    }
}


