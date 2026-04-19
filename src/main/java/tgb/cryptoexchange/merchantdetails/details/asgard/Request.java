package tgb.cryptoexchange.merchantdetails.details.asgard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class Request {

    private String orderId;

    private String merchantId;

    private Integer amount;

    private String callbackUri;

    @JsonProperty("currency")
    private String getCurrency() {
        return "RUB";
    }

    @JsonSerialize(using = Method.Serializer.class)
    private Method method;

}
