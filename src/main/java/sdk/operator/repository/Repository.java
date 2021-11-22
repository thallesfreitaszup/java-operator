package sdk.operator.repository;


import java.util.List;
import java.util.Optional;

public interface Repository {
    public List<Content> getContent(String url);
    public Content getTGZFromContent(List<Content> resources);
}
