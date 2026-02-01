package tgb.cryptoexchange.merchantdetails.details.studio;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {

    @JsonProperty("client_order_id")
    private String clientOrderId;

    private Integer amount;

    @JsonProperty("main_method")
    private String mainMethod;

    @JsonProperty("callback_url")
    private String callbackUrl;

}
