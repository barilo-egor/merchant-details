package net.rcetech.md.integrations.platform.cube;


import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantMethod;
import net.rcetech.md.meta.detailsapi.enums.RequestMethod;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum Method implements MerchantMethod {
    CARD("INTERBRUB", "Карта", RequestMethod.CARD),
    SBP("SBPRUB", "СБП", RequestMethod.SBP),
    SIM("MOBCOMRUB", "СИМ", null);

    @JsonValue
    private final String value;

    private final String description;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }
}

