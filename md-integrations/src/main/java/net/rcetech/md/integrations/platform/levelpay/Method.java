package net.rcetech.md.integrations.platform.levelpay;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantMethod;
import net.rcetech.md.meta.detailsapi.enums.RequestMethod;

import java.util.Optional;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    CARD("card", "Карта", RequestMethod.CARD),
    PHONE("phone", "СБП", RequestMethod.SBP),
    ALFA_ALFA("phone", "Альфа-альфа", null),
    SIM("sim", "SIM", null),
    QR("nspk", "QR", null);

    @JsonValue
    private final String value;

    private final String description;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }
}
