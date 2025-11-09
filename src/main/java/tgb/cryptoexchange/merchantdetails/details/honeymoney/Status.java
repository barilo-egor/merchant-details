package tgb.cryptoexchange.merchantdetails.details.honeymoney;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Status {
    PENDING("В процессе"),
    DENIED("Отклонено"),
    SUCCESSFUL("Успешно");

    private final String description;
}
