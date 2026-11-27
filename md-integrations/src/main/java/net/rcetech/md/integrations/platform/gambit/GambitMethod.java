package net.rcetech.md.integrations.platform.gambit;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.meta.detailsapi.enums.RequestMethod;

import java.util.Optional;

@AllArgsConstructor
@Getter
public enum GambitMethod implements Method {
    CARD("a0e3edeb-725a-4aec-ab9d-756e201659bb", "Карта", RequestMethod.CARD),
    SBP("a0e3edeb-773d-446d-8a06-48cb102ea182", "СБП", RequestMethod.SBP),
    MOBILE("a0e3edeb-80f0-4d5d-914e-064a94cde4e3", "Sim", null);

    @JsonValue
    final String methodUid;

    final String description;

    final RequestMethod requestMethod;

    @Override
    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }
}
