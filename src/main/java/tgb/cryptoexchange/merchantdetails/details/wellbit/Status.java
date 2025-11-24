package tgb.cryptoexchange.merchantdetails.details.wellbit;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;

import java.io.IOException;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    NEW("new", "Новый"),
    COMPLETE("complete", "Вывод выполнен"),
    CANCEL("cancel", "Вывод отменен"),
    CHARGE_BACK("chargeback", "По выводу произведён возврат средств");

    private final String value;

    private final String description;

    public static Status fromValue(String value) {
        for (Status wellBitStatus : Status.values()) {
            if (wellBitStatus.getValue().equals(value)) {
                return wellBitStatus;
            }
        }
        return null;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Deserializer extends JsonDeserializer<Status> {

        @Override
        public Status deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return Status.fromValue(p.getValueAsString());
        }
    }
}
