package net.rcetech.md.integrations.platform.crocopay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantMethod;
import net.rcetech.md.meta.detailsapi.enums.RequestMethod;

import java.util.Optional;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    TO_CARD("Карта", RequestMethod.CARD),
    SBP("СБП", RequestMethod.SBP),
    SIM("MOBILE_PHONE", null);

    private final String description;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }
}
