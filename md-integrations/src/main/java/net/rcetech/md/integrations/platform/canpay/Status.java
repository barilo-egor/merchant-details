package net.rcetech.md.integrations.platform.canpay;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

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
