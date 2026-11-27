package net.rcetech.md.integrations.platform.rspay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Request {

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("payment_method")
    private Method paymentMethod;

    private String amount;

    private Boolean receipt = false;

    @JsonProperty("callback_url")
    private String callbackUrl;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("kyc")
    public Boolean getKyc() {
        return false;
    }
}
