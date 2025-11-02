package tgb.cryptoexchange.merchantdetails.details.onlypays;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
public class Response {

    private boolean success;

    private String error;

    private Data data;

    @lombok.Data
    public static class Data {

        private String id;

        private String requisite;

        private String owner;

        private String bank;

        @JsonProperty("payment_type")
        @JsonDeserialize(using = Method.Deserializer.class)
        private Method method;
    }
}
