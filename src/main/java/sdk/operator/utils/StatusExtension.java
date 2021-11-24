package sdk.operator.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.kubernetes.client.openapi.models.V1Status;
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusExtension extends V1Status {

}
