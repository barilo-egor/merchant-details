package tgb.cryptoexchange.merchantdetails.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RequiredReceipt {
    NOT_REQUIRED("Не требуется"),
    ANY("Любой чек"),
    PDF("PDF чек");

    private final String description;

}

