package tgb.cryptoexchange.merchantdetails.details.onlypays;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Request {

    @JsonProperty("api_id")
    private String apiId;

    @JsonProperty("secret_key")
    private String secretKey;

    @JsonProperty("amount_rub")
    private Integer amount;

    @JsonProperty("payment_type")
    @JsonSerialize(using = Method.Serializer.class)
    private Method method;

    @JsonProperty("personal_id")
    private String personalId;

    @JsonProperty("sim")
    private Boolean sim;
}
