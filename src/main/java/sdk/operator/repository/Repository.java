package sdk.operator.repository;


import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface Repository {
    public List<Resource> getContent(String url);
    public Optional<Resource> GetValuesFromContent(List<Resource> resources, String componentName);
    public Resource getTGZFromContent(List<Resource> resources);
}
