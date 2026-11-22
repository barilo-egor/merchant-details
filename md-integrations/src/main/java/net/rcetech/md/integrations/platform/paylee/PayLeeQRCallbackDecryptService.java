package net.rcetech.md.integrations.platform.paylee;

import net.rcetech.md.integrations.properties.PayLeeQRProperties;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.commons.enums.Merchant;

@Service
public class PayLeeQRCallbackDecryptService extends PayLeeCallbackDecryptService {

    public PayLeeQRCallbackDecryptService(PayLeeQRProperties payLeeQRProperties) {
        super(payLeeQRProperties);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAY_LEE_QR;
    }
}
