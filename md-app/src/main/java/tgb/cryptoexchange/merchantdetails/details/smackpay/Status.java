package tgb.cryptoexchange.merchantdetails.details.smackpay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

@Getter
@AllArgsConstructor
public enum Status implements MerchantOrderStatus {

    AWAITING_KYC("awaiting_kyc", "Подтверждение личности"),
    KYC_PENDING("kyc_pending", "Проверка личности"),
    PROCESSING_LINK("processing_link", "Подготовка реквизитов"),
    PENDING("pending", "Ожидание"),
    SUCCEEDED("succeeded", "Успешно"),
    FAILED("failed", "Платёж не прошёл"),
    EXPIRED("expired", "Время на оплату истекло");

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
