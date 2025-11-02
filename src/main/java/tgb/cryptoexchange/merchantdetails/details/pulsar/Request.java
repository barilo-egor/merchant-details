package tgb.cryptoexchange.merchantdetails.details.pulsar;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class Request {

    private String orderId;

    private String merchantId;

    private Integer amount;

    @JsonSerialize(using = Method.Serializer.class)
    private Method method;

    private String userId;
}
