package tgb.cryptoexchange.merchantdetails.details.ezepay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Objects;

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

    public boolean isValid() {
        return this.getStatus().equals("success")
                && Objects.nonNull(this.getData())
                && (Objects.nonNull(this.getData().getBank()) || Objects.nonNull(this.getData().getBankSbp()))
                && Objects.nonNull(this.getData().getDetails())
                && Objects.nonNull(this.getData().getOrderId());
    }
}
