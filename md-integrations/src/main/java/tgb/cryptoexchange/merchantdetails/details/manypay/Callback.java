package tgb.cryptoexchange.merchantdetails.details.manypay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tgb.cryptoexchange.merchantdetails.details.UnwrappedCallback;

@EqualsAndHashCode(callSuper = true)
@Data
public class Callback extends UnwrappedCallback {

    @JsonProperty("order_uuid")
    private String id;

    private Status status;

}