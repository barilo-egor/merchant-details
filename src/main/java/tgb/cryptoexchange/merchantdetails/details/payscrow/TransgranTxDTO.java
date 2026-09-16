package tgb.cryptoexchange.merchantdetails.details.payscrow;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.List;
import java.util.Objects;

@Data
public class TransgranTxDTO {

    private Boolean success;

    @JsonProperty("orders")
    private Order order;

    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        Order.Item item = order.getItems().getFirst();
        if (Objects.isNull(item.orderId)) {
            result.notNull("item.orderId");
        }
        if (Objects.isNull(item.status)) {
            result.notNull("item.status");
        }
        return result;
    }

    public boolean hasDetails() {
        return Objects.nonNull(order) && !CollectionUtils.isEmpty(order.getItems());
    }

    @lombok.Data
    public static class Order {

        private List<Item> items;

        @lombok.Data
        public static class Item {

            @JsonProperty("client_order_id")
            private String orderId;

            @JsonDeserialize(using = Status.Deserializer.class)
            private Status status;

        }
    }

}
