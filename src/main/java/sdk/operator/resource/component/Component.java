package sdk.operator.resource.component;

import sdk.operator.integrations.repository.RepositoryType;

public class Component {
    private String name;

    private String chart;

    private RepositoryType provider;

    private String namespace;

    private String token;

    private String image;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getChart() {
        return chart;
    }

    public void setChart(String chart) {
        this.chart = chart;
    }

    public RepositoryType getProvider() {
        return provider;
    }

    public void setProvider(RepositoryType provider) {
        this.provider = provider;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return "Component{" +
                "name='" + name + '\'' +
                ", chart='" + chart + '\'' +
                ", provider=" + provider +
                ", namespace='" + namespace + '\'' +
                ", token='" + token + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
