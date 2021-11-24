package sdk.operator.resource.charlesdeployment;


import sdk.operator.resource.component.Component;

import java.util.List;

public class CharlesDeploymentSpec {
    private List<Component> components;

    private List<ChildResource> childResourceResources;

    public List<ChildResource> getChildResources() {
        return childResourceResources;
    }

    public void setChildResources(List<ChildResource> childResourceResources) {
        this.childResourceResources = childResourceResources;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }
}
