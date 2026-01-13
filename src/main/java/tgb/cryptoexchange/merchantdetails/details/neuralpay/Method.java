package tgb.cryptoexchange.merchantdetails.details.neuralpay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    ANY("Любой доступный метод"),
    NATIONAL_PAYMENT_SYSTEM("СБП, Mir Pay и т.п."),
    P2P_CARD("Перевод на карту"),
    P2P_CARD_CHECK("Перевод на карту с обязательной проверкой данных"),
    P2P_CARD_MONOBANK("Перевод на карту Monobank"),
    P2P_PHONE("Перевод по номеру телефона"),
    P2P_PHONE_CHECK("Перевод по номеру телефона с проверкой данных"),
    P2P_PHONE_MONOBANK("Перевод по номеру телефона в Monobank"),
    ACCOUNT_TRANSFER("Банковский перевод по реквизитам счета"),
    ACCOUNT_TRANSFER_MONOBANK("Банковский перевод на счет в Monobank"),
    CROSS_BORDER_CARD("Международный перевод с карты на карту"),
    CROSS_BORDER_PHONE("Международный перевод по номеру телефона")
    ;

    private final String description;
}
