package tgb.cryptoexchange.merchantdetails.details.mansory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {

    private Integer amount;

    @JsonProperty("payment_method")
    @JsonSerialize(using = Method.Serializer.class)
    private Method method;

    @JsonProperty("callback_url")
    private String callbackUrl;

}
