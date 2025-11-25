package tgb.cryptoexchange.merchantdetails.details.crocopay;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;

import java.io.IOException;

@Getter
@AllArgsConstructor
public enum Status implements MerchantOrderStatus {
    PENDING("Ожидание"),
    CANCELLED("Отменен"),
    SUCCESS("Платеж зачислен"),
    DISPUTE("Спор"),
    EXPIRED("Истекший");

    private final String description;

    public static Status fromValue(String value) {
        for (Status status : Status.values()) {
            if (status.name().equals(value.toUpperCase())) {
                return status;
            }
        }
        return null;
    }

    public static class Deserializer extends JsonDeserializer<Status> {

        @Override
        public Status deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return Status.fromValue(p.getValueAsString().toUpperCase());
        }
    }
}
