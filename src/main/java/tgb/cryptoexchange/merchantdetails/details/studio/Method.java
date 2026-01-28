package tgb.cryptoexchange.merchantdetails.details.studio;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    CARD("Оплата картой"),
    SBP("Система быстрых платежей"),
    SIM("Пополнение счета мобильного телефона");

    private final String description;
}
