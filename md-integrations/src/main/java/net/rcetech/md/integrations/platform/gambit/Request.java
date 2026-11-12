package net.rcetech.md.integrations.platform.gambit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.Currency;

@Data
public class Request {

    @JsonProperty("method_uid")
    @JsonSerialize(using = GambitMethod.Serializer.class)
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
