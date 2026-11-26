package net.rcetech.md.integrations.platform.canpay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

@Getter
@AllArgsConstructor
public enum Status implements MerchantOrderStatus {

    AWAITING_EMAIL("awaiting_email", "Ждём e-mail"),
    AWAITING_KYC("awaiting_kyc", "Ждём KYC"),
    KYC_PENDING("kyc_pending", "KYC проверка"),
    PROCESSING_LINK("processing_link", "Готовим QR"),
    PENDING("pending", "QR выдан"),
    SUCCEEDED("succeeded", "Оплачен"),
    FAILED("failed", "Ошибка"),
    EXPIRED("expired", "Истёк");

    private final String value;

    private final String description;

    public static Status getByValue(String value) {
        for (Status status : Status.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }

    public static class Deserializer extends ValueDeserializer<Status> {
        @Override
        public Status deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
            return Status.getByValue(jsonParser.getValueAsString());
        }
    }
}
