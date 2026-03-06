package tgb.cryptoexchange.merchantdetails.details;

import tgb.cryptoexchange.commons.enums.Merchant;

public interface CallbackDecryptService {

    Merchant getMerchant();

    String decrypt(String callbackBody);

}
