package tgb.cryptoexchange.merchantdetails.details.way2pay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonPropertyOrder(alphabetic = true)
public class Request {

    private String amount;

    private Integer bankId;

    @JsonProperty("callbackURL")
    private String callbackUrl;

    private Integer currencyId = 1;

    private String externalID;

    @JsonSerialize(using = Method.Serializer.class)
    private Method method;
}
