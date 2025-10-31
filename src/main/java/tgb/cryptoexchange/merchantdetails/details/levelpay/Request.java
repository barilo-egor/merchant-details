package tgb.cryptoexchange.merchantdetails.details.levelpay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
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
}
