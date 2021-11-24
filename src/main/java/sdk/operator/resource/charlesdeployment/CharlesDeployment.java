package sdk.operator.resource.charlesdeployment;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import io.fabric8.kubernetes.model.annotation.Kind;

import java.util.ArrayList;
import java.util.Locale;

@Kind("CharlesDeployment")
@Version("v1")
@Group("charlescd.io")
public class CharlesDeployment extends CustomResource<CharlesDeploymentSpec, CharlesDeploymentStatus> implements Namespaced {

    @Override
    public String toString() {
        return "CharlesDeployment{" +
                "status=" + status +
                "spec="+spec+
                '}';
    }

    public void updateStatus(boolean healthy) {
        if (this.status == null) {
            this.status = new CharlesDeploymentStatus();
        }
        this.status.setHealthy(true);
    }

    public void createChildren(String name, GenericKubernetesResource manifest) {
        if (this.spec.getChildResources() == null) {
            this.spec.setChildResources(new ArrayList<>());
        }
        this.spec.getChildResources().add(new ChildResource(manifest.getApiVersion(), manifest.getMetadata().getName(), manifest.getKind().toLowerCase(Locale.ROOT)+"s", name));
    }
}
