package sdk.operator.resource.charlesdeployment;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;

import java.util.List;

public class CharlesDeploymentStatus {
    private boolean healthy;
    private List<GenericKubernetesResource> children;

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public List<GenericKubernetesResource> getChildren() {
        return children;
    }

    public void setChildren(List<GenericKubernetesResource> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "CharlesDeploymentStatus{" +
                "healthy=" + healthy +
                ", children=" + children +
                '}';
    }
}
