package tgb.cryptoexchange.merchantdetails.details.asgard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;

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
