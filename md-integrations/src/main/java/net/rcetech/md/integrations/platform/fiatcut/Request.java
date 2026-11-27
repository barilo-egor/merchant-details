package net.rcetech.md.integrations.platform.fiatcut;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Request {

    @JsonProperty("external_id")
    private String externalId;

    private Integer amount;

    @JsonProperty("payment_detail_type")
    private Method method;

    @JsonProperty("merchant_id")
    private String merchantId;

    @JsonProperty("callback_url")
    private String callbackUrl;

    @JsonProperty("currency")
    private String currency;
}
