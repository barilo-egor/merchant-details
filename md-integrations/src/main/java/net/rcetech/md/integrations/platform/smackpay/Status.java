package net.rcetech.md.integrations.platform.smackpay;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@Getter
@AllArgsConstructor
public enum Status implements MerchantOrderStatus {

    AWAITING_KYC("awaiting_kyc", "Подтверждение личности"),
    AWAITING_EMAIL("awaiting_email", "Укажите e-mail"),
    KYC_PENDING("kyc_pending", "Проверка личности"),
    PROCESSING_LINK("processing_link", "Подготовка реквизитов"),
    PENDING("pending", "Ожидание"),
    SUCCEEDED("succeeded", "Успешно"),
    FAILED("failed", "Платёж не прошёл"),
    EXPIRED("expired", "Время на оплату истекло");

    private final String value;

    private final String description;

    @JsonCreator
    public static Status getByValue(String value) {
        for (Status status : Status.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
