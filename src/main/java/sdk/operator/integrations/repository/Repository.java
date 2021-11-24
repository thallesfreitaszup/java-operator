package sdk.operator.integrations.repository;


import sdk.operator.resource.component.Component;

import java.util.List;

public interface Repository {
    public List<Content> getContent(Component Component);
    public Content getTGZFromContent(List<Content> resources);
}
