package tgb.cryptoexchange.merchantdetails.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test & clear-merchants")
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
