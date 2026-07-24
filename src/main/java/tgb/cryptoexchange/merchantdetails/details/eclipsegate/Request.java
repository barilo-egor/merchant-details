package tgb.cryptoexchange.merchantdetails.details.eclipsegate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.Currency;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {

    @JsonProperty("client_id")
    private String clientId;

    private Integer amount;

    @JsonSerialize(using = Method.Serializer.class)
    private Method method;

    @JsonProperty("callback_url")
    private String callbackUrl;

    public String getCurrency() {
        return Currency.getInstance("RUB").getCurrencyCode();
    }

}
