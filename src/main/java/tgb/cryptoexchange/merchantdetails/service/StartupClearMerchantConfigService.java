package tgb.cryptoexchange.merchantdetails.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.List;

@Component
@Profile("!test")
public class StartupClearMerchantConfigService implements CommandLineRunner {

    private final MerchantConfigService merchantConfigService;

    public StartupClearMerchantConfigService(MerchantConfigService merchantConfigService) {
        this.merchantConfigService = merchantConfigService;
    }

    @Override
    public void run(String... args) {
        merchantConfigService.deleteAllByMerchantNotExist();
        merchantConfigService.resetMerchantOrder();
    }

}
