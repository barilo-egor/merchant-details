package tgb.cryptoexchange.merchantdetails.details.pandapay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.UUID;

@Data
public class Request {

    @JsonProperty("amount_rub")
    private Integer amount;

    @JsonProperty("idempotency_key")
    private String idempotencyKey = UUID.randomUUID().toString();

    @JsonProperty("requisite_type")
    @JsonSerialize(using = Method.Serializer.class)
    private Method method;
}
