package sdk.operator.resource.component;

import sdk.operator.repository.RepositoryType;

public class Component {
    private String name;

    private String tag;

    private String chart;

    private RepositoryType provider;

    private String namespace;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
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
}
