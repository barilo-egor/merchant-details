package tgb.cryptoexchange.merchantdetails.details.gambit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tgb.cryptoexchange.merchantdetails.details.UnwrappedCallback;

@EqualsAndHashCode(callSuper = true)
@Data
public class Callback extends UnwrappedCallback {

    @JsonDeserialize(using = Status.Deserializer.class)
    public Status status;
    @JsonProperty("session_id")
    private String id;

}
