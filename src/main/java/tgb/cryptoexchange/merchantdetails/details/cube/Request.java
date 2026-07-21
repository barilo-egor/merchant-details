package tgb.cryptoexchange.merchantdetails.details.cube;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class Request {

    private String amount;

    @JsonProperty("external_id")
    private String externalId;

    @JsonProperty("token")
    @JsonSerialize(using = Method.Serializer.class)
    private Method method;

    @JsonProperty("callback_url")
    private String callbackUrl;
}
