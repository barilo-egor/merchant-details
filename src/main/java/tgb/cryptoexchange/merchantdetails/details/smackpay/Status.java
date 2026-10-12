package tgb.cryptoexchange.merchantdetails.details.smackpay;

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

    public static class Deserializer extends JsonDeserializer<Status> {
        @Override
        public Status deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return Status.getByValue(jsonParser.getValueAsString());
        }
    }
}
