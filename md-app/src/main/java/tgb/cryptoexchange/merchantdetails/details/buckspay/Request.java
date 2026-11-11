package tgb.cryptoexchange.merchantdetails.details.buckspay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
public class Request {

    private String amount;

    private String shop;

    @JsonProperty("payment_type")
    @JsonSerialize(using = Method.Serializer.class)
    private Method paymentType;

    @JsonProperty("operation_id")
    private String operationId;

    private Integer bank;

    @JsonProperty("pair")
    public String getPair() {
        return "USDT-RUB";
    }

}
