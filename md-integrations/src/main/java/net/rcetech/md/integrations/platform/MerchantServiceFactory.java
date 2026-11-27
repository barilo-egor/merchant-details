package net.rcetech.md.integrations.platform;

import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.config.RestClientsConfig;
import net.rcetech.md.integrations.platform.bridgepay.BridgePayOrderCreationService;
import net.rcetech.md.integrations.properties.BridgePayAccountProperties;
import net.rcetech.md.integrations.service.SignatureService;
import net.rcetech.md.integrations.service.SleepingService;
import net.rcetech.md.meta.merchant.MerchantCatalog;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Создаёт сервисы площадок для аккаунтов без собственной логики: состав атрибутов берётся
 * из справочника мерчантов (площадка, префикс конфига), свойства привязываются по префиксу.
 * Аккаунт с отличающимся поведением по-прежнему описывается отдельным классом.
 */
@Component
public class MerchantServiceFactory {

    private final Environment environment;

    private final SignatureService signatureService;

    private final CallbackConfig callbackConfig;

    private final SleepingService sleepingService;

    private final Map<String, RestClient> restClientsByPrefix = new ConcurrentHashMap<>();

    public MerchantServiceFactory(Environment environment, SignatureService signatureService,
                                  CallbackConfig callbackConfig, SleepingService sleepingService) {
        this.environment = environment;
        this.signatureService = signatureService;
        this.callbackConfig = callbackConfig;
        this.sleepingService = sleepingService;
    }

    /**
     * Сервис мерчанта, если площадка поддерживает аккаунты без собственного класса.
     */
    public Optional<MerchantOrderCreationService> create(Merchant merchant) {
        MerchantCatalog catalog = MerchantCatalog.of(merchant);
        return switch (catalog.getPlatform()) {
            case BRIDGEPAY -> Optional.of(bridgePay(catalog));
            default -> Optional.empty();
        };
    }

    private BridgePayOrderCreationService bridgePay(MerchantCatalog catalog) {
        BridgePayAccountProperties properties = bind(catalog);
        return new BridgePayOrderCreationService(Merchant.valueOf(catalog.name()),
                restClient(catalog, properties.url()), properties,
                signatureService, callbackConfig, sleepingService);
    }

    private BridgePayAccountProperties bind(MerchantCatalog catalog) {
        String prefix = catalog.getConfigPrefix();
        BridgePayAccountProperties properties = Binder.get(environment)
                .bind(prefix, Bindable.of(BridgePayAccountProperties.class))
                .orElse(null);
        if (properties == null || properties.url() == null || properties.url().isBlank()) {
            throw new IllegalStateException(
                    "В конфиге нет группы '%s' для мерчанта %s (площадка %s)".formatted(
                            prefix, catalog.name(), catalog.getPlatform()));
        }
        return properties;
    }

    private RestClient restClient(MerchantCatalog catalog, String url) {
        return restClientsByPrefix.computeIfAbsent(catalog.getConfigPrefix(),
                prefix -> RestClientsConfig.get30SecondsReadTimeoutRestClientBuilder().baseUrl(url).build());
    }
}
