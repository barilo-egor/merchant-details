package tgb.cryptoexchange.merchantdetails.details.manypay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Request {

    /**
     * В копейках
     */
    private Integer amount;

    @JsonProperty("payment_method")
    private Method paymentMethod;

}
