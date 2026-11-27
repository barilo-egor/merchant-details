package net.rcetech.md.integrations.platform.smackpay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;
import net.rcetech.md.integrations.platform.UnwrappedCallback;

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

        private Status status;
    }

}
