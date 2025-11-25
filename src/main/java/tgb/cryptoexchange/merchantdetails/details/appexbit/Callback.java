package tgb.cryptoexchange.merchantdetails.details.appexbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tgb.cryptoexchange.merchantdetails.details.UnwrappedCallback;

@EqualsAndHashCode(callSuper = true)
@Data
public class Callback extends UnwrappedCallback {

    @JsonProperty("offerId")
    private String id;

    @JsonDeserialize(using = Status.Deserializer.class)
    @JsonProperty("status")
    private Status status;
}
