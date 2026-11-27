package net.rcetech.md.app.service;

import net.rcetech.md.integrations.platform.smackpay.SmackPayClientIds;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.Optional;

@Service
public class SmackPayClientIdService implements SmackPayClientIds {

    private final MerchantClientIdService merchantClientIdService;

    public SmackPayClientIdService(MerchantClientIdService merchantClientIdService) {
        this.merchantClientIdService = merchantClientIdService;
    }

    @Override
    public Optional<String> findOrAssignClientId(String userId) {
        return merchantClientIdService.findOrAssign(Merchant.SMACK_PAY, userId);
    }
}
