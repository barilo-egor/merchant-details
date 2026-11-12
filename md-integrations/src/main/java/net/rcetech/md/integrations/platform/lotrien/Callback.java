package net.rcetech.md.integrations.platform.lotrien;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.rcetech.md.integrations.platform.UnwrappedCallback;

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
