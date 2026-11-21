package net.rcetech.md.integrations.platform;

import tgb.cryptoexchange.commons.enums.Merchant;

public interface CallbackDecryptService {

    Merchant getMerchant();

    String decrypt(String callbackBody);

}
