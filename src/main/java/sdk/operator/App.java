package sdk.operator;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.kubernetes.client.openapi.ApiException;
import sdk.operator.controller.CharlesDeploymentController;
import sdk.operator.resource.charlesdeployment.CharlesDeployment;
import sdk.operator.resource.charlesdeployment.CharlesDeploymentList;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
public class App 
{
    public static final Logger logger = Logger.getLogger(App.class.getName());
    public static void main( String[] args ) throws IOException, InterruptedException, ApiException {
        try (final KubernetesClient client = new DefaultKubernetesClient()) {
            SharedInformerFactory informerFactory = client.informers();
            MixedOperation<CharlesDeployment, CharlesDeploymentList, Resource<CharlesDeployment>> charlesDeploymentClient =
                    client.customResources(CharlesDeployment.class, CharlesDeploymentList.class);
            SharedIndexInformer<CharlesDeployment> charlesDeploymentInformer = informerFactory.sharedIndexInformerForCustomResource(CharlesDeployment.class, 10 * 60 * 1000);
            SharedIndexInformer<Deployment> deploymentIndexInformer = informerFactory.sharedIndexInformerFor(Deployment.class, 10 * 60 * 1000);
            SharedIndexInformer<Service> serviceInformer = informerFactory.sharedIndexInformerFor(Service.class, 10 * 60 * 1000);
            CharlesDeploymentController charlesDeploymentController = new CharlesDeploymentController(
                    deploymentIndexInformer,
                    charlesDeploymentInformer,
                    serviceInformer,
                    client,
                    charlesDeploymentClient);
            charlesDeploymentController.create();
            Future<Void> startedInformersFuture = informerFactory.startAllRegisteredInformers();
            startedInformersFuture.get();
            charlesDeploymentController.run();

            }catch(KubernetesClientException | ExecutionException exception ) {
            logger.log(Level.SEVERE, "Kubernetes Client Exception : " + exception.getMessage());
        }
    }

}
