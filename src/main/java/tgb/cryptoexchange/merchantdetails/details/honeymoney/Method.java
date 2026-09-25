package tgb.cryptoexchange.merchantdetails.details.honeymoney;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;

import java.util.Optional;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    CARD("Карта", null, "/v2/merchant/transactions", RequestMethod.CARD),
    SBP("СБП", null, "/v2/merchant/transactions/sbp", RequestMethod.SBP),
    CROSS_BORDER("Трансгран", null, "/v2/merchant/transactions/cross-border", null),
    SBER_ACCOUNT("Сбер номер счета", "СберБанк", "/v2/merchant/transactions/account", null);

    private final String description;

    private final String bank;

    private final String uri;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }
}
