package tgb.cryptoexchange.merchantdetails.details.cashout;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonPropertyOrder(alphabetic = true)
public class Request {

    private Integer amount;

    @JsonSerialize(using = Method.Serializer.class)
    @JsonProperty("paymentMethod")
    private Method method;

    @JsonProperty("currency")
    public String getCurrency() {
        return "RUB";
    }
}
