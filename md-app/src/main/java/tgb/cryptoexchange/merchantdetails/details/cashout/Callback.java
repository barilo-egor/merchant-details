package tgb.cryptoexchange.merchantdetails.details.cashout;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tgb.cryptoexchange.merchantdetails.details.UnwrappedCallback;
import tools.jackson.databind.annotation.JsonDeserialize;

@EqualsAndHashCode(callSuper = true)
@Data
public class Callback extends UnwrappedCallback {

    @JsonProperty("transactionId")
    private String id;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

}