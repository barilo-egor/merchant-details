package tgb.cryptoexchange.merchantdetails.details.yolo;

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
    CREATED("Создана"),
    PENDING("В ожидании"),
    COMPLETED("Завершена"),
    FAILED("Ошибка"),
    CANCELED("Отменена"),
    EXPIRED("Истек срок"),
    DISPUTE("Апелляция");

    private final String description;

    public static class Deserializer extends JsonDeserializer<Status> {
        @Override
        public Status deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return Status.valueOf(jsonParser.getText());
        }
    }

}
