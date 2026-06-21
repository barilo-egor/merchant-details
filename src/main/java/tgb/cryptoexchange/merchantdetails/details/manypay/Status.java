package tgb.cryptoexchange.merchantdetails.details.manypay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    PENDING("Создан, ожидает подтверждения"),
    WAITING_PAYMENT("Подтверждён, ожидает оплаты"),
    PAID("Оплата получена"),
    CANCELLED("Отменён"),
    EXPIRED("Истёк");

    private final String description;

}
