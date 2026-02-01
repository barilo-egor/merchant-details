package tgb.cryptoexchange.merchantdetails.details.studio;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {

    @JsonProperty("client_order_id")
    private String clientOrderId;

    private Integer amount;

    @JsonSerialize(using = Method.Serializer.class)
    @JsonProperty("main_method")
    private Method mainMethod;

    @JsonProperty("callback_url")
    private String callbackUrl;

}
