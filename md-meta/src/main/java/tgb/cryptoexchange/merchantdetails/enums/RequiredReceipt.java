package tgb.cryptoexchange.merchantdetails.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@Getter
public enum RequiredReceipt {
    NOT_REQUIRED("Не требуется"),
    ANY("Любой чек"),
    PDF("PDF чек");

    public static final List<RequiredReceipt> REQUIRED_RECEIPTS = Arrays.stream(values())
            .filter(r -> !NOT_REQUIRED.equals(r))
            .toList();

    private final String description;

}

