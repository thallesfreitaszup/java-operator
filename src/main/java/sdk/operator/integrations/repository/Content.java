package sdk.operator.integrations.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Content {

    private String name;
    @JsonProperty("download_url")
    private String downloadUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public String toString() {
        return "Content{" +
                "name='" + name + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                '}';
    }
}
