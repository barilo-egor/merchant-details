package tgb.cryptoexchange.merchantdetails.details.gambit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;
import tgb.cryptoexchange.merchantdetails.details.UnwrappedCallback;

@EqualsAndHashCode(callSuper = true)
@Data
public class Callback extends UnwrappedCallback {

    private CallbackData data;

    @Override
    public String getId() {
        return data.getId();
    }

    @Override
    public MerchantOrderStatus getStatus() {
        return data.getStatus();
    }

    @Data
    public static class CallbackData {

        @JsonProperty("merchant_order_id")
        private String id;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;
    }

}
