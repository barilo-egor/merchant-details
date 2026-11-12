package net.rcetech.md.integrations.platform.levelpay;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {

    @JsonProperty("external_id")
    private String externalId;

    private Integer amount;

    @JsonProperty("payment_detail_type")
    @JsonSerialize(using = Method.Serializer.class)
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
    public String getCurrency() {
        return "rub";
    }
}
