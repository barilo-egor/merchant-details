package tgb.cryptoexchange.merchantdetails.details.noros;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {

    private String orderId;

    @JsonProperty("bankId")
    @JsonSerialize(using = Method.Serializer.class)
    private Method paymentMethod;

    private Integer amount;
}
