package tgb.cryptoexchange.merchantdetails.details.buckspay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tgb.cryptoexchange.merchantdetails.details.UnwrappedCallback;

@EqualsAndHashCode(callSuper = true)
@Data
public class Callback extends UnwrappedCallback {

    @JsonProperty("invoice_id")
    private String id;

    @JsonDeserialize(using = Status.Deserializer.class)
    @JsonProperty("state")
    private Status status;

}
