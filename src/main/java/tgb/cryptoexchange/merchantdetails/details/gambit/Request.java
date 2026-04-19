package tgb.cryptoexchange.merchantdetails.details.gambit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.Currency;

@Data
public class Request {

    @JsonProperty("method_uid")
    @JsonSerialize(using = Method.Serializer.class)
    private Method method;

    private Integer amount;

    @JsonProperty("terminal_uid")
    private String terminalUid;

    @JsonProperty("merchant_order_id")
    private String orderId;

    public String getType() {
        return "payin";
    }

    public String getCurrency() {
        return Currency.getInstance("RUB").getCurrencyCode();
    }
}
