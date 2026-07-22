package tgb.cryptoexchange.merchantdetails.details.rspay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class Request {

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("payment_method")
    @JsonSerialize(using = Method.Serializer.class)
    private Method paymentMethod;

    private String amount;

    private Boolean receipt = false;

    @JsonProperty("callback_url")
    private String callbackUrl;

    @JsonProperty("currency")
    public String getCurrency() {
        return "RUB";
    }

    @JsonProperty("kyc")
    public Boolean getKyc() {
        return true;
    }
}
