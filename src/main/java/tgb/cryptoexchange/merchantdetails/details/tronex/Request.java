package tgb.cryptoexchange.merchantdetails.details.tronex;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class Request {

    @JsonProperty("id")
    private String orderId;

    private Integer amount;

    @JsonProperty("paymethod")
    @JsonSerialize(using = Method.Serializer.class)
    private Method method;

    @JsonProperty("callback_url")
    private String callbackUrl;

}
