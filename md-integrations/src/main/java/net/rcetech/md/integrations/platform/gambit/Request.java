package net.rcetech.md.integrations.platform.gambit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Request {

    @JsonProperty("method_uid")
    private Method method;

    private Integer amount;

    @JsonProperty("terminal_uid")
    private String terminalUid;

    @JsonProperty("merchant_order_id")
    private String orderId;

    private String type = "payin";

    private String currency;
}
