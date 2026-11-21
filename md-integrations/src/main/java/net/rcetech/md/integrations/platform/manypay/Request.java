package net.rcetech.md.integrations.platform.manypay;

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
