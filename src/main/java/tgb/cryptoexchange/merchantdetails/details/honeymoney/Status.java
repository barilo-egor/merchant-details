package tgb.cryptoexchange.merchantdetails.details.honeymoney;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    PENDING("В процессе"),
    DENIED("Отклонено"),
    SUCCESSFUL("Успешно");

    private final String description;
}
