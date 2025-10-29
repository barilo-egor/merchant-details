package tgb.cryptoexchange.merchantdetails.details.bitzone;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

@AllArgsConstructor
@Getter
public enum Status {
    PENDING("В ожидании"),
    ACTIVE("Активный"),
    CHECKING("Проверка"),
    DISPUTE("Спор"),
    RE_CALCULATION("Перерасчёт"),
    CLOSED("Закрыт"),
    CANCELED("Отменён");

    private final String description;

    public static class Deserializer extends JsonDeserializer<Status> {
        @Override
        public Status deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return Status.valueOf(p.getValueAsString().toUpperCase());
        }
    }
}
