package net.rcetech.md.integrations.platform.levelpay;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {

    @JsonProperty("external_id")
    private String externalId;

    private Integer amount;

    @JsonProperty("payment_detail_type")
    private Method paymentDetailType;

    @JsonProperty("merchant_id")
    private String merchantId;

    @JsonProperty("callback_url")
    private String callbackUrl;

    @JsonProperty("is_floating_amount")
    private Boolean floatingAmount;

    @JsonProperty("payment_gateway")
    private String paymentGateway;

    @JsonProperty("currency")
    private String currency;
}
