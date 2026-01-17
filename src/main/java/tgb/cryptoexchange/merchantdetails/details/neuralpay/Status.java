package tgb.cryptoexchange.merchantdetails.details.neuralpay;

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
    PENDING("В ожидании"),
    AWAITING_FUNDS("В ожидании средств"),
    CHARGED("Завершена"),
    FAILED("Не завершена"),
    PROCESSING("В процессе"),
    CANCELLED("Транзакция отменена"),
    DISPUTE_IN_PROGRESS("Возврат средств");

    private final String description;

    public static class Deserializer extends JsonDeserializer<Status> {
        @Override
        public Status deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return Status.valueOf(jsonParser.getText());
        }
    }
}
