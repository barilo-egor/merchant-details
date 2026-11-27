package net.rcetech.md.meta.merchant;

import net.rcetech.md.meta.enums.FiatCurrency;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.EnumMap;
import java.util.Map;

/**
 * Справочник мерчантов: единственный источник данных о мерчанте — площадка, валюта,
 * префикс конфига. Покрытие 1:1 с enum Merchant из commons.
 * Валюта определяется аккаунтом мерчанта у провайдера; при заведении тенгового мерчанта
 * меняется только строка справочника.
 */
public enum MerchantCatalog {

    // --- bridgepay ---
    ALFA_TEAM(Platform.BRIDGEPAY, FiatCurrency.RUB, "alfa-team"),
    ALFA_TEAM_QR(Platform.BRIDGEPAY, FiatCurrency.RUB, "alfa-team-qr"),
    ALFA_TEAM_WT(Platform.BRIDGEPAY, FiatCurrency.RUB, "alfa-team-white-triangle"),
    // --- asgard ---
    ASGARD(Platform.ASGARD, FiatCurrency.RUB, "asgard"),
    ASGARD_HIGH_CHECK(Platform.ASGARD, FiatCurrency.RUB, "asgard-high-check"),
    ASGARD_SIM(Platform.ASGARD, FiatCurrency.RUB, "asgard-sim"),
    ASGARD_WT(Platform.ASGARD, FiatCurrency.RUB, "asgard-wt"),
    // --- crocopay ---
    BASE_51(Platform.CROCOPAY, FiatCurrency.RUB, "base51"),
    BASE_51_HIGH_CHECK(Platform.CROCOPAY, FiatCurrency.RUB, "base51-high-check"),
    BASE_51_LOW_CHECK(Platform.CROCOPAY, FiatCurrency.RUB, "base51-low-check"),
    BASE_51_SIM(Platform.CROCOPAY, FiatCurrency.RUB, "base51-sim"),
    // --- baypay ---
    BAY_PAY(Platform.BAYPAY, FiatCurrency.RUB, "bay-pay"),
    BAY_PAY_HIGH_CHECK(Platform.BAYPAY, FiatCurrency.RUB, "bay-pay-high-check"),
    BAY_PAY_LOW_CHECK(Platform.BAYPAY, FiatCurrency.RUB, "bay-pay-low-check"),
    // --- paybox ---
    BIT_BOLIDE(Platform.PAYBOX, FiatCurrency.RUB, "bit-bolide"),
    BIT_BOLIDE_DEEP_LINK(Platform.PAYBOX, FiatCurrency.RUB, "bit-bolide-deep-link"),
    BIT_BOLIDE_WT(Platform.PAYBOX, FiatCurrency.RUB, "bit-bolide-wt"),
    // --- bitzone ---
    BIT_ZONE(Platform.BITZONE, FiatCurrency.RUB, "bit-zone"),
    // --- paybox ---
    BUCKS_PAY(Platform.PAYBOX, FiatCurrency.RUB, "bucks-pay"),
    BUCKS_PAY_SIM(Platform.PAYBOX, FiatCurrency.RUB, "bucks-pay-sim"),
    // --- canpay ---
    CANPAY(Platform.CANPAY, FiatCurrency.RUB, "can-pay"),
    // --- cashout ---
    CASH_OUT(Platform.CASHOUT, FiatCurrency.RUB, "cashout"),
    // --- crocopay ---
    CROCO_PAY(Platform.CROCOPAY, FiatCurrency.RUB, "croco-pay"),
    // --- cube ---
    CUBE(Platform.CUBE, FiatCurrency.RUB, "cube"),
    CUBE_HIGH_CHECK(Platform.CUBE, FiatCurrency.RUB, "cube-high-check"),
    CUBE_LOW_CHECK(Platform.CUBE, FiatCurrency.RUB, "cube-low-check"),
    CUBE_SIM(Platform.CUBE, FiatCurrency.RUB, "cube-sim"),
    // --- bridgepay ---
    DEORA(Platform.BRIDGEPAY, FiatCurrency.RUB, "deora"),
    DEORA_LOW_CHECK(Platform.BRIDGEPAY, FiatCurrency.RUB, "deora-low-check"),
    DEORA_PDF(Platform.BRIDGEPAY, FiatCurrency.RUB, "deora-pdf"),
    DEORA_SIM(Platform.BRIDGEPAY, FiatCurrency.RUB, "deora-sim"),
    // --- evopay ---
    EVO_PAY(Platform.EVOPAY, FiatCurrency.RUB, "evo-pay"),
    // --- bridgepay ---
    EXTASY_PAY(Platform.BRIDGEPAY, FiatCurrency.RUB, "extasy-pay"),
    EXTASY_PAY_PDF(Platform.BRIDGEPAY, FiatCurrency.RUB, "extasy-pay-pdf"),
    EXTASY_PAY_QR_NSPK(Platform.BRIDGEPAY, FiatCurrency.RUB, "extasy-pay-qr-nspk"),
    EXTASY_PAY_QR_VIET(Platform.BRIDGEPAY, FiatCurrency.RUB, "extasy-pay-qr-viet"),
    // --- fiatcut ---
    FIAT_CUT(Platform.FIATCUT, FiatCurrency.RUB, "fiat-cut"),
    // --- gambit ---
    GAMBIT(Platform.GAMBIT, FiatCurrency.RUB, "gambit"),
    GAMBIT_SIM(Platform.GAMBIT, FiatCurrency.RUB, "gambit-sim"),
    // --- bridgepay ---
    GEO_TRANSFER(Platform.BRIDGEPAY, FiatCurrency.RUB, "geo-transfer"),
    // --- goatx ---
    GOAT_X(Platform.GOATX, FiatCurrency.RUB, "goatx"),
    GOAT_X_SIM(Platform.GOATX, FiatCurrency.RUB, "goatx-sim"),
    // --- paybox ---
    HELLBIT(Platform.PAYBOX, FiatCurrency.RUB, "hell-bit"),
    HELLBIT_BT(Platform.PAYBOX, FiatCurrency.RUB, "hell-bit-bt"),
    HELLBIT_RECEIPT(Platform.PAYBOX, FiatCurrency.RUB, "hell-bit-receipt"),
    // --- gambit ---
    HESOYAM(Platform.GAMBIT, FiatCurrency.RUB, "hesoyam"),
    HESOYAM_SIM(Platform.GAMBIT, FiatCurrency.RUB, "hesoyam-sim"),
    // --- honeymoney ---
    HONEY_MONEY(Platform.HONEYMONEY, FiatCurrency.RUB, "honey-money"),
    // --- lotrien ---
    LOTRIEN(Platform.LOTRIEN, FiatCurrency.RUB, "lotrien"),
    LOTRIEN_PDF(Platform.LOTRIEN, FiatCurrency.RUB, "lotrien"),
    // --- manypay ---
    MANY_PAY(Platform.MANYPAY, FiatCurrency.RUB, "many-pay"),
    MANY_PAY_HIGH_CHECK(Platform.MANYPAY, FiatCurrency.RUB, "many-pay-high-check"),
    MANY_PAY_LOW_CHECK(Platform.MANYPAY, FiatCurrency.RUB, "many-pay-low-check"),
    // --- levelpay ---
    MERIDIAN_PAY(Platform.LEVELPAY, FiatCurrency.RUB, "meridian-pay"),
    MERIDIAN_PAY_HIGH_CHECK(Platform.LEVELPAY, FiatCurrency.RUB, "meridian-pay-high-check"),
    MERIDIAN_PAY_LOW_CHECK(Platform.LEVELPAY, FiatCurrency.RUB, "meridian-pay-low-check"),
    MERIDIAN_PAY_NSPK(Platform.LEVELPAY, FiatCurrency.RUB, "meridian-pay-nspk"),
    MERIDIAN_PAY_SIM(Platform.LEVELPAY, FiatCurrency.RUB, "meridian-pay-sim"),
    // --- noros ---
    NOROS(Platform.NOROS, FiatCurrency.RUB, "noros"),
    NOROS_HIGH_CHECK(Platform.NOROS, FiatCurrency.RUB, "noros-high-check"),
    // --- onlypays ---
    ONLY_PAYS(Platform.ONLYPAYS, FiatCurrency.RUB, "only-pays"),
    // --- bridgepay ---
    ONYX_PAY(Platform.BRIDGEPAY, FiatCurrency.RUB, "onyx-pay"),
    // --- payscrow ---
    PAYSCROW(Platform.PAYSCROW, FiatCurrency.RUB, "payscrow"),
    PAYSCROW_HIGH_CHECK(Platform.PAYSCROW, FiatCurrency.RUB, "payscrow-high-check"),
    PAYSCROW_LOW(Platform.PAYSCROW, FiatCurrency.RUB, "payscrow-low"),
    PAYSCROW_SIM(Platform.PAYSCROW, FiatCurrency.RUB, "payscrow-sim"),
    PAYSCROW_TRANSGRAN(Platform.PAYSCROW, FiatCurrency.RUB, "payscrow-transgran"),
    PAYSCROW_WHITE_TRIANGLE(Platform.PAYSCROW, FiatCurrency.RUB, "payscrow-white-triangle"),
    // --- paysync ---
    PAYSYNC(Platform.PAYSYNC, FiatCurrency.RUB, "paysync"),
    // --- paylee ---
    PAY_LEE(Platform.PAYLEE, FiatCurrency.RUB, "pay-lee"),
    PAY_LEE_QR(Platform.PAYLEE, FiatCurrency.RUB, "pay-lee-qr"),
    // --- levelpay ---
    PLATA_18(Platform.LEVELPAY, FiatCurrency.RUB, "plata18"),
    PLATA_PAYMENT(Platform.LEVELPAY, FiatCurrency.RUB, "plata-payment"),
    // --- paybox ---
    PRIME_WALLET(Platform.PAYBOX, FiatCurrency.RUB, "prime-wallet"),
    PRIME_WALLET_HIGH_CHECK(Platform.PAYBOX, FiatCurrency.RUB, "prime-wallet-high-check"),
    PRIME_WALLET_LOW_CHECK(Platform.PAYBOX, FiatCurrency.RUB, "prime-wallet-low-check"),
    // --- prismapay ---
    PRISMA_PAY(Platform.PRISMAPAY, FiatCurrency.RUB, "prisma-pay"),
    // --- paybox ---
    PW_PAY(Platform.PAYBOX, FiatCurrency.RUB, "pw-pay"),
    // --- bridgepay ---
    ROSTRAST(Platform.BRIDGEPAY, FiatCurrency.RUB, "rostrast"),
    // --- rspay ---
    RS_PAY(Platform.RSPAY, FiatCurrency.RUB, "rs-pay"),
    RS_PAY_BT(Platform.RSPAY, FiatCurrency.RUB, "rs-pay-bt"),
    // --- settlex ---
    SETTLE_X(Platform.SETTLEX, FiatCurrency.RUB, "settle-x"),
    SETTLE_X_15(Platform.SETTLEX, FiatCurrency.RUB, "settle-x15"),
    // --- smackpay ---
    SMACK_PAY(Platform.SMACKPAY, FiatCurrency.RUB, "smack-pay"),
    // --- bridgepay ---
    SOUZ(Platform.BRIDGEPAY, FiatCurrency.RUB, "souz"),
    SOUZ_PDF(Platform.BRIDGEPAY, FiatCurrency.RUB, "souz-pdf"),
    SOUZ_SBP_QR(Platform.BRIDGEPAY, FiatCurrency.RUB, "souz-sbp-qr"),
    SOUZ_SIM(Platform.BRIDGEPAY, FiatCurrency.RUB, "souz-sim"),
    STORM_TRADE(Platform.BRIDGEPAY, FiatCurrency.RUB, "storm-trade"),
    STORM_TRADE_13(Platform.BRIDGEPAY, FiatCurrency.RUB, "storm-trade13"),
    // --- tronex ---
    TRON_EX_BT(Platform.TRONEX, FiatCurrency.RUB, "tron-ex-bt"),
    TRON_EX_DEEP_LINK(Platform.TRONEX, FiatCurrency.RUB, "tron-ex-dl"),
    TRON_EX_PDF(Platform.TRONEX, FiatCurrency.RUB, "tron-ex-pdf"),
    TRON_EX_QR(Platform.TRONEX, FiatCurrency.RUB, "tron-ex-qr"),
    TRON_EX_SIM(Platform.TRONEX, FiatCurrency.RUB, "tron-ex-sim"),
    // --- viatrum ---
    VIATRUM(Platform.VIATRUM, FiatCurrency.RUB, "viatrum"),
    // --- wat ---
    WAT(Platform.WAT, FiatCurrency.RUB, "wat"),
    WAT_PDF(Platform.WAT, FiatCurrency.RUB, "wat-pdf"),
    WAT_SIM(Platform.WAT, FiatCurrency.RUB, "wat-sim"),
    // --- yolo ---
    YOLO(Platform.YOLO, FiatCurrency.RUB, "yolo"),
    YOLO_SIM(Platform.YOLO, FiatCurrency.RUB, "yolo-sim")

    ;

    private final Platform platform;
    private final FiatCurrency currency;
    private final String configPrefix;

    private static final Map<Merchant, MerchantCatalog> BY_MERCHANT = new EnumMap<>(Merchant.class);

    static {
        for (MerchantCatalog entry : values()) {
            BY_MERCHANT.put(Merchant.valueOf(entry.name()), entry);
        }
    }

    MerchantCatalog(Platform platform, FiatCurrency currency, String configPrefix) {
        this.platform = platform;
        this.currency = currency;
        this.configPrefix = configPrefix;
    }

    public static MerchantCatalog of(Merchant merchant) {
        MerchantCatalog entry = BY_MERCHANT.get(merchant);
        if (entry == null) {
            throw new IllegalArgumentException("Нет записи справочника для мерчанта " + merchant);
        }
        return entry;
    }

    public Platform getPlatform() {
        return platform;
    }

    public FiatCurrency getCurrency() {
        return currency;
    }

    public String getConfigPrefix() {
        return configPrefix;
    }
}
