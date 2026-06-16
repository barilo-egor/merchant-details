package tgb.cryptoexchange.merchantdetails.details.tmpay;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;

import java.io.IOException;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    CREATED("Created", "Инвойс создан, ожидает подбора реквизита"),
    WAITING_PAYMENT("WaitingPayment", "Реквизит выдан, клиент должен оплатить"),
    COMPLETED("Completed", "Платёж подтверждён, средства зачислены"),
    CANCELED("Canceled", "Отменён или истёк срок оплаты"),
    DISPUTE("Dispute", "Открыт диспут по платежу");

    private final String value;

    private final String description;

    public static Status fromValue(String v) {
        for (Status c : Status.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        return null;
    }

    public static class Deserializer extends JsonDeserializer<Status> {

        @Override
        public Status deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return Status.fromValue(jsonParser.getValueAsString());
        }
    }
}
