package tgb.cryptoexchange.merchantdetails.details.buckspay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class Request {

    private String amount;

    private String shop;

    @JsonProperty("payment_type")
    @JsonSerialize(using = Method.Serializer.class)
    private Method paymentType;

    @JsonProperty("operation_id")
    private String operationId;

    @JsonProperty("pair")
    public String getPair() {
        return "USDT-RUB";
    }

    @JsonProperty("bank")
    public Integer getBank() {
        return 0;
    }


}
