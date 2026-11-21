package net.rcetech.md.integrations.platform.gambit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;
import net.rcetech.md.integrations.platform.UnwrappedCallback;
import tools.jackson.databind.annotation.JsonDeserialize;

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
