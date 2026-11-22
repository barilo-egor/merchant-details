package net.rcetech.md.integrations.platform.paylee;

import net.rcetech.md.integrations.properties.PayLeePropertiesImpl;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.commons.enums.Merchant;

@Service
public class PayLeeCallbackDecryptServiceImpl extends PayLeeCallbackDecryptService {

    public PayLeeCallbackDecryptServiceImpl(PayLeePropertiesImpl payLeeProperties) {
        super(payLeeProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAY_LEE;
    }
}
