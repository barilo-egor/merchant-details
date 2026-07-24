package tgb.cryptoexchange.merchantdetails.details.paycrown;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class Request {

    private Integer amount;

    @JsonProperty("callback_url")
    private String callbackUrl;

    private String currency = "rub";

    @JsonProperty("created_at")
    private Long createdAt;

    @JsonProperty("merchant_id")
    private String merchantId;

    @JsonProperty("payment_type")
    @JsonSerialize(using = Method.Serializer.class)
    private Method method;

}
