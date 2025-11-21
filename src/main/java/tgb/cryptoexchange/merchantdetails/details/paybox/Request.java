package tgb.cryptoexchange.merchantdetails.details.paybox;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Request {

    @JsonProperty("merchant_transaction_id")
    private String merchantTransactionId;

    private Integer amount;

    @JsonProperty("currency")
    public String getCurrency() {
        return "RUB";
    }
}
