package net.rcetech.md.integrations.platform.yolo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

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

    public static class Deserializer extends ValueDeserializer<Status> {
        @Override
        public Status deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
            return Status.valueOf(jsonParser.getString());
        }
    }

}
