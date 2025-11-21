package tgb.cryptoexchange.merchantdetails.details.nicepay;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class Request {

    @JsonProperty("merchant_id")
    private String merchantId;

    @JsonProperty("secret")
    private String secret;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("customer_ip")
    private String customerIp = "qwe";

    private String customer;

    private Integer amount;

    @JsonSerialize(using = Method.Serializer.class)
    private Method method;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String methodSBP;

    @JsonProperty("currency")
    public String getCurrency() {
        return "RUB";
    }
}
