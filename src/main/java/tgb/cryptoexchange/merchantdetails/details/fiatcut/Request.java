package tgb.cryptoexchange.merchantdetails.details.fiatcut;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class Request {

    @JsonProperty("external_id")
    private String externalId;

    private Integer amount;

    @JsonSerialize(using = Method.Serializer.class)
    @JsonProperty("payment_detail_type")
    private Method method;

    @JsonProperty("merchant_id")
    private String merchantId;

    @JsonProperty("callback_url")
    private String callbackUrl;

    @JsonProperty("currency")
    public String getCurrency() {
        return "rub";
    }
}
