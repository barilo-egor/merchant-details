package tgb.cryptoexchange.merchantdetails.details.settlex;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

@AllArgsConstructor
@Getter
public enum Status {
    CREATED("Транзакция создана и ожидает обработки"),
    IN_PROGRESS("Транзакция в процессе обработки трейдером"),
    READY("Транзакция готова к завершению"),
    MILK("Специальный статус обработки"),
    DISPUTE("По транзакции открыт спор"),
    EXPIRED("Время на выполнение транзакции истекло"),
    CANCELED("Транзакция отменена");

    private final String description;

    public static class Deserializer extends JsonDeserializer<Status> {
        @Override
        public Status deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return Status.valueOf(jsonParser.getText());
        }
    }
}
