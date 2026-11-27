package net.rcetech.md.app.service;

import net.rcetech.md.integrations.platform.canpay.CanPayClientIds;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.Optional;

@Service
public class CanPayClientIdService implements CanPayClientIds {

    private final MerchantClientIdService merchantClientIdService;

    public CanPayClientIdService(MerchantClientIdService merchantClientIdService) {
        this.merchantClientIdService = merchantClientIdService;
    }

    @Override
    public Optional<String> findOrAssignClientId(String userId) {
        return merchantClientIdService.findOrAssign(Merchant.CANPAY, userId);
    }
}
