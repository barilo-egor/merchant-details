package net.rcetech.md.integrations.platform.bitzone;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    PENDING("В ожидании"),
    ACTIVE("Активный"),
    CHECKING("Проверка"),
    DISPUTE("Спор"),
    RE_CALCULATION("Перерасчёт"),
    CLOSED("Закрыт"),
    CANCELED("Отменён");

    private final String description;

    public static class Deserializer extends ValueDeserializer<Status> {
        @Override
        public Status deserialize(JsonParser p, DeserializationContext ctxt) {
            return Status.valueOf(p.getValueAsString().toUpperCase());
        }
    }
}
