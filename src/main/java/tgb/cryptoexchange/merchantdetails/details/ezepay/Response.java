package tgb.cryptoexchange.merchantdetails.details.ezepay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Response {

    private Data data;

    private String status;

    private String message;

    @lombok.Data
    public static class Data {

        @JsonProperty("uniqkey")
        private String orderId;

        @JsonProperty("props_name")
        private String bank;

        @JsonProperty("props_sbp")
        private String bankSbp;

        @JsonProperty("props")
        private String details;
    }
}
