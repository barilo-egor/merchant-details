package net.rcetech.md.integrations.platform;

import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.Optional;

public interface MerchantService {

    Merchant getMerchant();

    Optional<DetailsResponse> createOrder(OrderCreationRequest detailsRequest);

    Optional<CallbackStatus> parseCallback(String callbackBody);

    void deleteReceipt(String orderId, String orderStatus);

    Optional<String> fetchCallbackDataByTransactionId(String transactionId);

    void cancelOrder(CancelOrderRequest cancelOrderRequest);

    void sendReceipt(String orderId, byte[] fileContent, String fileName);
}
