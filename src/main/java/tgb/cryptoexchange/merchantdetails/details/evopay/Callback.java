package tgb.cryptoexchange.merchantdetails.details.evopay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tgb.cryptoexchange.merchantdetails.details.UnwrappedCallback;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Callback extends UnwrappedCallback {

    @JsonProperty("order_status")
    private Status status;

    private Order order;

    @Override
    public String getId() {
        return order.getId();
    }

    @Data
    public static class Order {
        private String id;
    }
}
