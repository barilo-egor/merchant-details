package tgb.cryptoexchange.merchantdetails.details;

import java.util.Optional;

/**
 * Класс заглушка на время миграции коллбеков в микросервис.
 */
public class MerchantCallbackMock implements MerchantCallback {
    @Override
    public Optional<String> getMerchantOrderId() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getStatus() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getStatusDescription() {
        return Optional.empty();
    }
}
