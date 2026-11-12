package net.rcetech.md.integrations.platform.levelpay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.MeridianPayNspkProperties;

@Service
public class MeridianPayNspkOrderCreationService extends LevelPayOrderCreationService {

    protected MeridianPayNspkOrderCreationService(@Qualifier("meridianPayNspkRestClient") RestClient restClient,
                                                  MeridianPayNspkProperties meridianPayProperties,
                                                  CallbackConfig callbackConfig) {
        super(restClient, callbackConfig, meridianPayProperties);
    }

    @Override
    protected Integer getAmount(String amount) {
        return Integer.parseInt(amount) / 100;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.MERIDIAN_PAY_NSPK;
    }
}
