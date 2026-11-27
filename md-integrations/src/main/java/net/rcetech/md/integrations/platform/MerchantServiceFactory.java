package net.rcetech.md.integrations.platform;

import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.config.RestClientsConfig;
import net.rcetech.md.integrations.platform.bridgepay.BridgePayOrderCreationService;
import net.rcetech.md.integrations.platform.asgard.AsgardOrderCreationService;
import net.rcetech.md.integrations.platform.crocopay.CrocoPayOrderCreationService;
import net.rcetech.md.integrations.platform.cube.CubeOrderCreationService;
import net.rcetech.md.integrations.platform.levelpay.LevelPayOrderCreationService;
import net.rcetech.md.integrations.platform.paybox.PayBoxOrderCreationService;
import net.rcetech.md.integrations.platform.lotrien.LotrienOrderCreationService;
import net.rcetech.md.integrations.platform.noros.NorosOrderCreationService;
import net.rcetech.md.integrations.platform.payscrow.PayscrowOrderCreationService;
import net.rcetech.md.integrations.platform.settlex.SettleXOrderCreationService;
import net.rcetech.md.integrations.platform.tronex.TronExOrderCreationService;
import net.rcetech.md.integrations.platform.wat.WatOrderCreationService;
import net.rcetech.md.integrations.properties.BridgePayAccountProperties;
import net.rcetech.md.integrations.properties.AsgardAccountProperties;
import net.rcetech.md.integrations.properties.CrocoPayAccountProperties;
import net.rcetech.md.integrations.properties.CubeAccountProperties;
import net.rcetech.md.integrations.properties.LevelPayAccountProperties;
import net.rcetech.md.integrations.properties.LotrienAccountProperties;
import net.rcetech.md.integrations.properties.NorosAccountProperties;
import net.rcetech.md.integrations.properties.PayscrowAccountProperties;
import net.rcetech.md.integrations.properties.SettleXAccountProperties;
import net.rcetech.md.integrations.properties.TronExAccountProperties;
import net.rcetech.md.integrations.properties.WatAccountProperties;
import net.rcetech.md.integrations.platform.baypay.BayPayOrderCreationService;
import net.rcetech.md.integrations.platform.manypay.ManyPayOrderCreationService;
import net.rcetech.md.integrations.platform.paylee.PayLeeMerchantService;
import net.rcetech.md.integrations.platform.goatx.GoatxMerchantOrderCreationService;
import net.rcetech.md.integrations.platform.gambit.GambitOrderCreationService;
import net.rcetech.md.integrations.platform.rspay.RSPayOrderCreationService;
import net.rcetech.md.integrations.platform.yolo.YoloOrderCreationService;
import net.rcetech.md.integrations.properties.BayPayAccountProperties;
import net.rcetech.md.integrations.properties.ManyPayAccountProperties;
import net.rcetech.md.integrations.properties.PayLeeAccountProperties;
import net.rcetech.md.integrations.properties.GoatxAccountProperties;
import net.rcetech.md.integrations.properties.GambitAccountProperties;
import net.rcetech.md.integrations.properties.RSPayBTAccountProperties;
import net.rcetech.md.integrations.properties.YoloAccountProperties;
import net.rcetech.md.integrations.properties.PayBoxAccountProperties;
import net.rcetech.md.integrations.service.ReceiptService;
import net.rcetech.md.integrations.service.SignatureService;
import net.rcetech.md.integrations.service.SleepingService;
import net.rcetech.md.meta.merchant.MerchantCatalog;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

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

    private final ReceiptService receiptService;

    private final Map<String, RestClient> restClientsByPrefix = new ConcurrentHashMap<>();

    public MerchantServiceFactory(Environment environment, SignatureService signatureService,
                                  CallbackConfig callbackConfig, SleepingService sleepingService,
                                  ReceiptService receiptService) {
        this.environment = environment;
        this.signatureService = signatureService;
        this.callbackConfig = callbackConfig;
        this.sleepingService = sleepingService;
        this.receiptService = receiptService;
    }

    /**
     * Сервис мерчанта, если площадка поддерживает аккаунты без собственного класса.
     */
    public Optional<MerchantOrderCreationService> create(Merchant merchant) {
        MerchantCatalog catalog = MerchantCatalog.of(merchant);
        return switch (catalog.getPlatform()) {
            case BRIDGEPAY -> Optional.of(bridgePay(catalog));
            case LEVELPAY -> Optional.of(levelPay(catalog));
            case ASGARD -> Optional.of(asgard(catalog));
            case CROCOPAY -> Optional.of(crocoPay(catalog));
            case CUBE -> Optional.of(cube(catalog));
            case PAYBOX -> Optional.of(payBox(catalog));
            case BAYPAY -> Optional.of(bayPay(catalog));
            case MANYPAY -> Optional.of(manyPay(catalog));
            case PAYLEE -> Optional.of(payLee(catalog));
            case GOATX -> Optional.of(goatx(catalog));
            case GAMBIT -> Optional.of(gambit(catalog));
            case RSPAY -> rsPayBt(catalog);
            case YOLO -> Optional.of(yolo(catalog));
            case PAYSCROW -> Optional.of(payscrow(catalog));
            case TRONEX -> tronEx(catalog);
            case WAT -> Optional.of(wat(catalog));
            case NOROS -> Optional.of(noros(catalog));
            case SETTLEX -> Optional.of(settleX(catalog));
            case LOTRIEN -> lotrien(catalog);
            default -> Optional.empty();
        };
    }

    private BridgePayOrderCreationService bridgePay(MerchantCatalog catalog) {
        BridgePayAccountProperties properties = bind(catalog, BridgePayAccountProperties.class, BridgePayAccountProperties::url);
        return new BridgePayOrderCreationService(Merchant.valueOf(catalog.name()),
                restClient(catalog, properties.url()), properties,
                signatureService, callbackConfig, sleepingService);
    }

    private LevelPayOrderCreationService levelPay(MerchantCatalog catalog) {
        LevelPayAccountProperties properties = bind(catalog, LevelPayAccountProperties.class, LevelPayAccountProperties::url);
        return new LevelPayOrderCreationService(Merchant.valueOf(catalog.name()),
                restClient(catalog, properties.url()), callbackConfig, properties);
    }

    private AsgardOrderCreationService asgard(MerchantCatalog catalog) {
        AsgardAccountProperties properties = bind(catalog, AsgardAccountProperties.class, AsgardAccountProperties::url);
        return new AsgardOrderCreationService(Merchant.valueOf(catalog.name()),
                restClient(catalog, properties.url()), properties, signatureService, callbackConfig);
    }

    private CrocoPayOrderCreationService crocoPay(MerchantCatalog catalog) {
        CrocoPayAccountProperties properties = bind(catalog, CrocoPayAccountProperties.class, CrocoPayAccountProperties::url);
        return new CrocoPayOrderCreationService(Merchant.valueOf(catalog.name()),
                restClient(catalog, properties.url()), properties, callbackConfig);
    }

    private CubeOrderCreationService cube(MerchantCatalog catalog) {
        CubeAccountProperties properties = bind(catalog, CubeAccountProperties.class, CubeAccountProperties::url);
        return new CubeOrderCreationService(Merchant.valueOf(catalog.name()),
                restClient(catalog, properties.url()), properties, callbackConfig);
    }

    private PayBoxOrderCreationService payBox(MerchantCatalog catalog) {
        PayBoxAccountProperties properties = bind(catalog, PayBoxAccountProperties.class, PayBoxAccountProperties::url);
        return new PayBoxOrderCreationService(Merchant.valueOf(catalog.name()),
                restClient(catalog, properties.url()), properties);
    }

    private BayPayOrderCreationService bayPay(MerchantCatalog catalog) {
        BayPayAccountProperties properties = bind(catalog, BayPayAccountProperties.class, BayPayAccountProperties::url);
        return new BayPayOrderCreationService(Merchant.valueOf(catalog.name()),
                restClient(catalog, properties.url()), properties, callbackConfig);
    }

    private ManyPayOrderCreationService manyPay(MerchantCatalog catalog) {
        ManyPayAccountProperties properties = bind(catalog, ManyPayAccountProperties.class, ManyPayAccountProperties::url);
        return new ManyPayOrderCreationService(Merchant.valueOf(catalog.name()),
                restClient(catalog, properties.url()), properties, callbackConfig);
    }

    private PayLeeMerchantService payLee(MerchantCatalog catalog) {
        PayLeeAccountProperties properties = bind(catalog, PayLeeAccountProperties.class, PayLeeAccountProperties::url);
        return new PayLeeMerchantService(Merchant.valueOf(catalog.name()),
                restClient(catalog, properties.url()), properties);
    }

    private GoatxMerchantOrderCreationService goatx(MerchantCatalog catalog) {
        GoatxAccountProperties properties = bind(catalog, GoatxAccountProperties.class, GoatxAccountProperties::url);
        return new GoatxMerchantOrderCreationService(Merchant.valueOf(catalog.name()),
                restClient(catalog, properties.url()), properties);
    }

    private GambitOrderCreationService gambit(MerchantCatalog catalog) {
        GambitAccountProperties properties = bind(catalog, GambitAccountProperties.class, GambitAccountProperties::url);
        return new GambitOrderCreationService(Merchant.valueOf(catalog.name()),
                restClient(catalog, properties.url()), properties);
    }

    /**
     * RS_PAY живёт собственным классом RSPayOrderCreationServiceImpl (override body()); фабрика
     * создаёт только аккаунт rs-pay-bt. У группы rs-pay-bt нет url — RestClient строится
     * по url группы rs-pay (так же работал общий бин rsPayRestClient).
     */
    private Optional<MerchantOrderCreationService> rsPayBt(MerchantCatalog catalog) {
        RSPayBTAccountProperties properties = bind(catalog, RSPayBTAccountProperties.class);
        String rsPayPrefix = MerchantCatalog.RS_PAY.getConfigPrefix();
        String url = environment.getProperty(rsPayPrefix + ".url");
        if (url == null || url.isBlank()) {
            throw new IllegalStateException(
                    "В конфиге нет url группы '%s' для мерчанта %s (площадка %s)".formatted(
                            rsPayPrefix, catalog.name(), catalog.getPlatform()));
        }
        return Optional.of(new RSPayOrderCreationService(Merchant.valueOf(catalog.name()),
                restClient(catalog, url), properties, callbackConfig, signatureService));
    }

    private YoloOrderCreationService yolo(MerchantCatalog catalog) {
        YoloAccountProperties properties = bind(catalog, YoloAccountProperties.class, YoloAccountProperties::url);
        return new YoloOrderCreationService(Merchant.valueOf(catalog.name()),
                restClient(catalog, properties.url()), properties, callbackConfig);
    }

    private PayscrowOrderCreationService payscrow(MerchantCatalog catalog) {
        PayscrowAccountProperties properties = bind(catalog, PayscrowAccountProperties.class, PayscrowAccountProperties::url);
        return new PayscrowOrderCreationService(Merchant.valueOf(catalog.name()),
                restClient(catalog, properties.url()), properties);
    }

    /**
     * TRON_EX_DEEP_LINK живёт собственным классом TronExDeepLinkCreationService (override buildResponse()).
     * У групп tron-ex-bt/qr/pdf нет url — RestClient строится по url группы tron-ex-sim
     * (так же работал общий бин tronExRestClient).
     */
    private Optional<MerchantOrderCreationService> tronEx(MerchantCatalog catalog) {
        TronExAccountProperties properties = bind(catalog, TronExAccountProperties.class);
        String simPrefix = MerchantCatalog.TRON_EX_SIM.getConfigPrefix();
        String url = environment.getProperty(simPrefix + ".url");
        if (url == null || url.isBlank()) {
            throw new IllegalStateException(
                    "В конфиге нет url группы '%s' для мерчанта %s (площадка %s)".formatted(
                            simPrefix, catalog.name(), catalog.getPlatform()));
        }
        return Optional.of(new TronExOrderCreationService(Merchant.valueOf(catalog.name()),
                restClient(catalog, url), properties, callbackConfig));
    }

    /**
     * У групп wat-sim/wat-pdf нет url — RestClient строится по url группы wat
     * (так же работал общий бин watRestClient).
     */
    private WatOrderCreationService wat(MerchantCatalog catalog) {
        WatAccountProperties properties = bind(catalog, WatAccountProperties.class);
        String watPrefix = MerchantCatalog.WAT.getConfigPrefix();
        String url = environment.getProperty(watPrefix + ".url");
        if (url == null || url.isBlank()) {
            throw new IllegalStateException(
                    "В конфиге нет url группы '%s' для мерчанта %s (площадка %s)".formatted(
                            watPrefix, catalog.name(), catalog.getPlatform()));
        }
        return new WatOrderCreationService(Merchant.valueOf(catalog.name()),
                restClient(catalog, url), properties, callbackConfig, receiptService);
    }

    private NorosOrderCreationService noros(MerchantCatalog catalog) {
        NorosAccountProperties properties = bind(catalog, NorosAccountProperties.class, NorosAccountProperties::url);
        return new NorosOrderCreationService(Merchant.valueOf(catalog.name()),
                restClient(catalog, properties.url()), properties);
    }

    private SettleXOrderCreationService settleX(MerchantCatalog catalog) {
        SettleXAccountProperties properties = bind(catalog, SettleXAccountProperties.class, SettleXAccountProperties::url);
        return new SettleXOrderCreationService(Merchant.valueOf(catalog.name()),
                restClient(catalog, properties.url()), properties, callbackConfig);
    }

    /** LOTRIEN_PDF живёт собственным классом LotrienPDFOrderCreationService (override sendReceipt()). */
    private Optional<MerchantOrderCreationService> lotrien(MerchantCatalog catalog) {
        LotrienAccountProperties properties = bind(catalog, LotrienAccountProperties.class, LotrienAccountProperties::url);
        return Optional.of(new LotrienOrderCreationService(Merchant.valueOf(catalog.name()),
                restClient(catalog, properties.url()), properties));
    }

    /** Привязка свойств без проверки url — для аккаунтов, чей RestClient строится по чужой группе. */
    private <T> T bind(MerchantCatalog catalog, Class<T> type) {
        String prefix = catalog.getConfigPrefix();
        T properties = Binder.get(environment)
                .bind(prefix, Bindable.of(type))
                .orElse(null);
        if (properties == null) {
            throw new IllegalStateException(
                    "В конфиге нет группы '%s' для мерчанта %s (площадка %s)".formatted(
                            prefix, catalog.name(), catalog.getPlatform()));
        }
        return properties;
    }

    private <T> T bind(MerchantCatalog catalog, Class<T> type, Function<T, String> url) {
        String prefix = catalog.getConfigPrefix();
        T properties = Binder.get(environment)
                .bind(prefix, Bindable.of(type))
                .orElse(null);
        if (properties == null || url.apply(properties) == null || url.apply(properties).isBlank()) {
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
