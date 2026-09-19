package tgb.cryptoexchange.merchantdetails.details.smackpay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;
import tgb.cryptoexchange.merchantdetails.details.UnwrappedCallback;

import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
public class Callback extends UnwrappedCallback {

    private Payment payment;

    @Override
    public String getId() {
        return Objects.nonNull(payment) ? payment.getId() : null;
    }

    @Override
    public MerchantOrderStatus getStatus() {
        return Objects.nonNull(payment) ? payment.getStatus() : null;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payment {
        private String id;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;
    }

}
