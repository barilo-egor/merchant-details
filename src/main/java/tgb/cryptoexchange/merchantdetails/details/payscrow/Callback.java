package tgb.cryptoexchange.merchantdetails.details.payscrow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantCallback;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Callback implements MerchantCallback {

    private Payload payload;

    @Override
    public String getMerchantOrderId() {
        return getPayload().getId();
    }

    @Override
    public String getStatus() {
        return getPayload().getStatus().name();
    }

    @Override
    public String getStatusDescription() {
        return getPayload().getStatus().getDescription();
    }

    @Data
    public static class Payload {

        private String id;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;
    }
}
