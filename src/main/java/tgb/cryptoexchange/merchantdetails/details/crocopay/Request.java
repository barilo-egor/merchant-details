package tgb.cryptoexchange.merchantdetails.details.crocopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Request {

    private Integer amount;

    @JsonProperty("payment_option")
    private Method method;

    @JsonProperty("currency")
    public String getCurrency() {
        return "RUB";
    }
}
