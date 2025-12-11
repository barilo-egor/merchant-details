package tgb.cryptoexchange.merchantdetails.details.settlex;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.serialize.DateSerializer;

import java.time.LocalDateTime;

@Data
public class Request {

    private Integer amount;

    private String orderId;

    private String method;

    @JsonProperty("expired_at")
    @JsonSerialize(using = DateSerializer.ISO8601.class)
    private LocalDateTime expiredAt;

    private String callbackUri;
}
