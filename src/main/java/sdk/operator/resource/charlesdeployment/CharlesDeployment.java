package sdk.operator.resource.charlesdeployment;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import io.fabric8.kubernetes.model.annotation.Kind;
@Kind("CharlesDeployment")
@Version("v1")
@Group("charlescd.io")
public class CharlesDeployment extends CustomResource<CharlesDeploymentSpec, CharlesDeploymentStatus> implements Namespaced {
}
