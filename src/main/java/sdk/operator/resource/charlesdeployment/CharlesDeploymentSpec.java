package sdk.operator.resource.charlesdeployment;


import sdk.operator.resource.component.Component;

import java.util.List;

public class CharlesDeploymentSpec {
    private List<Component> components;

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }
}
