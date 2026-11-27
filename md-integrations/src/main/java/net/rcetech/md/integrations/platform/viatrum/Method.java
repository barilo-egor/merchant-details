package net.rcetech.md.integrations.platform.viatrum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantMethod;
import net.rcetech.md.meta.detailsapi.enums.RequestMethod;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    VTB_QR("VTB_QR", "ВТБ QR", 5),
    SBER_QR("SBER_QR", "Сбер QR", 3),
    ALFA_QR("ALFA_QR", "Альфа QR", 6),
    OTP_QR("OTP_QR", "ОТП QR", 13),
    GAZPROM_QR("GAZPROM_QR", "Газпром Банк QR", 12),
    PSB_QR("PSB_QR", "ПСБ QR", 2);

    private final String value;

    private final String description;

    private final Integer id;

    @JsonCreator
    public static Method fromValue(String value) {
        for (Method method : Method.values()) {
            if (method.getValue().equals(value)) {
                return method;
            }
        }
        return null;
    }

    @Override
    public Optional<RequestMethod> getRequestMethod() {
        return Optional.empty();
    }

    /**
     * Wire-формат площадки: для всех QR-методов уходит строка "PAYMENT_LINK".
     */
    @JsonValue
    public String getJsonValue() {
        if (Arrays.asList(VTB_QR, SBER_QR, ALFA_QR, OTP_QR, GAZPROM_QR, PSB_QR).contains(this)) {
            return "PAYMENT_LINK";
        }
        return getValue();
    }
}
