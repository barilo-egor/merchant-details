package tgb.cryptoexchange.merchantdetails.details.settlex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tgb.cryptoexchange.merchantdetails.details.UnwrappedCallback;
import tools.jackson.databind.annotation.JsonDeserialize;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Callback extends UnwrappedCallback {

    @JsonProperty("internalId")
    private String id;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;
}
