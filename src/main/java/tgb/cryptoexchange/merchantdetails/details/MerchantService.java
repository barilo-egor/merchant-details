package tgb.cryptoexchange.merchantdetails.details;

import org.springframework.web.multipart.MultipartFile;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.Optional;

public interface MerchantService {

    Merchant getMerchant();

    Optional<DetailsResponse> createOrder(DetailsRequest detailsRequest);

    void updateStatus(String callbackBody);

    void cancelOrder(CancelOrderRequest cancelOrderRequest);

    void sendReceipt(String orderId, MultipartFile multipartFile);
}
