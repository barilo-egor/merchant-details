package tgb.cryptoexchange.merchantdetails.details;

/**
 * Класс заглушка на время миграции коллбеков в микросервис.
 */
public class MerchantCallbackMock implements MerchantCallback {
    @Override
    public String getMerchantOrderId() {
        return "order_id";
    }

    @Override
    public String getStatus() {
        return "status";
    }

    @Override
    public String getStatusDescription() {
        return "status_description";
    }
}
