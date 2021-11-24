package sdk.operator.resource.charlesdeployment;

public class ChildResource {

    public ChildResource() {

    }

    @Override
    public String toString() {
        return "ChildResource{" +
                "apiVersion='" + apiVersion + '\'' +
                ", name='" + name + '\'' +
                ", plural='" + plural + '\'' +
                ", componentName='" + componentName + '\'' +
                '}';
    }

    public ChildResource(String apiVersion, String name, String plural, String componentName) {
        this.apiVersion = apiVersion;
        this.name = name;
        this.plural = plural;
        this.componentName = componentName;
    }
    private String apiVersion;
    private String name;

    private String plural;
    private String componentName;

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getPlural() {
        return plural;
    }

    public void setPlural(String plural) {
        this.plural = plural;
    }
}
