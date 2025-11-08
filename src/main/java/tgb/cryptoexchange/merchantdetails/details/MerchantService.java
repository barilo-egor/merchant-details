package tgb.cryptoexchange.merchantdetails.details;

import tgb.cryptoexchange.merchantdetails.enums.Merchant;

import java.util.Optional;

public interface MerchantService {

    Merchant getMerchant();

    Optional<DetailsResponse> createOrder(DetailsRequest detailsRequest);
}
