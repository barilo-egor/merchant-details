package tgb.cryptoexchange.merchantdetails.details.lotrien;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tgb.cryptoexchange.merchantdetails.details.UnwrappedCallback;
import tgb.cryptoexchange.merchantdetails.details.evopay.Status;

@EqualsAndHashCode(callSuper = true)
@Data
public class Callback extends UnwrappedCallback {

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
