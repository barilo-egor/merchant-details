package net.rcetech.md.integrations.platform;

import net.rcetech.md.meta.detailsapi.enums.RequestMethod;
import net.rcetech.md.meta.merchant.MerchantCatalog;
import org.springframework.stereotype.Component;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Реестр площадок: у каждой площадки — свои методы и статусы. Данные о мерчанте
 * выводятся из справочника: площадка даёт полный набор, справочник может сузить
 * его ({@link MerchantCatalog#methodNames()}).
 * Выбор метода по категории для площадок с несколькими методами одной категории
 * задан явно ({@link #explicitCategoryMethods}) — порядок объявления констант
 * enum'а на результат не влияет.
 */
@Component
public class PlatformRegistry {

    /** paybox: поиск идёт по версиям с чеками — задано явно, а не «последним в enum». */
    private static final Map<RequestMethod, String> PAYBOX_CATEGORY_METHODS = Map.of(
            RequestMethod.CARD, "CARD_RECEIPT",
            RequestMethod.SBP, "SBP_RECEIPT");

    public List<MerchantOrderStatus> statuses(Merchant merchant) {
        return List.of(switch (MerchantCatalog.of(merchant).getPlatform()) {
            case ASGARD -> net.rcetech.md.integrations.platform.asgard.Status.values();
            case BAYPAY -> net.rcetech.md.integrations.platform.baypay.Status.values();
            case BITZONE -> net.rcetech.md.integrations.platform.bitzone.Status.values();
            case BRIDGEPAY -> net.rcetech.md.integrations.platform.bridgepay.Status.values();
            case CANPAY -> net.rcetech.md.integrations.platform.canpay.Status.values();
            case CASHOUT -> net.rcetech.md.integrations.platform.cashout.Status.values();
            case CROCOPAY -> net.rcetech.md.integrations.platform.crocopay.Status.values();
            case CUBE -> net.rcetech.md.integrations.platform.cube.Status.values();
            case EVOPAY -> net.rcetech.md.integrations.platform.evopay.Status.values();
            case FIATCUT -> net.rcetech.md.integrations.platform.fiatcut.Status.values();
            case GAMBIT -> net.rcetech.md.integrations.platform.gambit.Status.values();
            case GOATX -> net.rcetech.md.integrations.platform.goatx.Status.values();
            case HONEYMONEY -> net.rcetech.md.integrations.platform.honeymoney.Status.values();
            case LEVELPAY -> net.rcetech.md.integrations.platform.levelpay.Status.values();
            case LOTRIEN -> net.rcetech.md.integrations.platform.lotrien.Status.values();
            case MANYPAY -> net.rcetech.md.integrations.platform.manypay.Status.values();
            case NOROS -> net.rcetech.md.integrations.platform.noros.Status.values();
            case ONLYPAYS -> net.rcetech.md.integrations.platform.onlypays.Status.values();
            case PAYBOX -> net.rcetech.md.integrations.platform.paybox.Status.values();
            case PAYLEE -> net.rcetech.md.integrations.platform.paylee.Status.values();
            case PAYSCROW -> net.rcetech.md.integrations.platform.payscrow.Status.values();
            case PAYSYNC -> net.rcetech.md.integrations.platform.paysync.Status.values();
            case PRISMAPAY -> net.rcetech.md.integrations.platform.prismapay.Status.values();
            case RSPAY -> net.rcetech.md.integrations.platform.rspay.Status.values();
            case SETTLEX -> net.rcetech.md.integrations.platform.settlex.Status.values();
            case SMACKPAY -> net.rcetech.md.integrations.platform.smackpay.Status.values();
            case TRONEX -> net.rcetech.md.integrations.platform.tronex.Status.values();
            case VIATRUM -> net.rcetech.md.integrations.platform.viatrum.Status.values();
            case WAT -> net.rcetech.md.integrations.platform.wat.Status.values();
            case YOLO -> net.rcetech.md.integrations.platform.yolo.Status.values();
        });
    }

    public List<MerchantMethod> methods(Merchant merchant) {
        List<MerchantMethod> platformMethods = List.of(switch (MerchantCatalog.of(merchant).getPlatform()) {
            case ASGARD -> net.rcetech.md.integrations.platform.asgard.Method.values();
            case BAYPAY -> net.rcetech.md.integrations.platform.baypay.Method.values();
            case BITZONE -> net.rcetech.md.integrations.platform.bitzone.Method.values();
            case BRIDGEPAY -> net.rcetech.md.integrations.platform.bridgepay.Method.values();
            case CANPAY -> net.rcetech.md.integrations.platform.canpay.Method.values();
            case CASHOUT -> net.rcetech.md.integrations.platform.cashout.Method.values();
            case CROCOPAY -> net.rcetech.md.integrations.platform.crocopay.Method.values();
            case CUBE -> net.rcetech.md.integrations.platform.cube.Method.values();
            case EVOPAY -> net.rcetech.md.integrations.platform.evopay.Method.values();
            case FIATCUT -> net.rcetech.md.integrations.platform.fiatcut.Method.values();
            // У gambit/hesoyam разные наборы методов (methodUid'ы) — разбиение по мерчанту.
            case GAMBIT -> merchant == Merchant.HESOYAM || merchant == Merchant.HESOYAM_SIM
                    ? net.rcetech.md.integrations.platform.gambit.HesoyamMethod.values()
                    : net.rcetech.md.integrations.platform.gambit.GambitMethod.values();
            case GOATX -> net.rcetech.md.integrations.platform.goatx.Method.values();
            case HONEYMONEY -> net.rcetech.md.integrations.platform.honeymoney.Method.values();
            case LEVELPAY -> net.rcetech.md.integrations.platform.levelpay.Method.values();
            case LOTRIEN -> net.rcetech.md.integrations.platform.lotrien.Method.values();
            case MANYPAY -> net.rcetech.md.integrations.platform.manypay.Method.values();
            case NOROS -> net.rcetech.md.integrations.platform.noros.Method.values();
            case ONLYPAYS -> net.rcetech.md.integrations.platform.onlypays.Method.values();
            case PAYBOX -> net.rcetech.md.integrations.platform.paybox.Method.values();
            case PAYLEE -> net.rcetech.md.integrations.platform.paylee.Method.values();
            case PAYSCROW -> net.rcetech.md.integrations.platform.payscrow.Method.values();
            case PAYSYNC -> net.rcetech.md.integrations.platform.paysync.Method.values();
            case PRISMAPAY -> net.rcetech.md.integrations.platform.prismapay.Method.values();
            case RSPAY -> net.rcetech.md.integrations.platform.rspay.Method.values();
            case SETTLEX -> net.rcetech.md.integrations.platform.settlex.Method.values();
            case SMACKPAY -> net.rcetech.md.integrations.platform.smackpay.Method.values();
            case TRONEX -> net.rcetech.md.integrations.platform.tronex.Method.values();
            case VIATRUM -> net.rcetech.md.integrations.platform.viatrum.Method.values();
            case WAT -> net.rcetech.md.integrations.platform.wat.Method.values();
            case YOLO -> net.rcetech.md.integrations.platform.yolo.Method.values();
        });
        List<String> override = MerchantCatalog.of(merchant).methodNames();
        if (override.isEmpty()) {
            return platformMethods;
        }
        Map<String, MerchantMethod> byName = platformMethods.stream()
                .collect(Collectors.toMap(MerchantMethod::name, Function.identity()));
        return override.stream()
                .map(name -> {
                    MerchantMethod method = byName.get(name);
                    if (method == null) {
                        throw new IllegalStateException(
                                "Метод %s из справочника отсутствует у площадки %s".formatted(
                                        name, MerchantCatalog.of(merchant).getPlatform()));
                    }
                    return method;
                })
                .toList();
    }

    /**
     * Явное соответствие «категория → метод» для площадок, у которых несколько методов
     * одной категории. Пусто — берётся первый метод категории в объявлении платформы.
     */
    public Map<RequestMethod, String> explicitCategoryMethods(Merchant merchant) {
        return MerchantCatalog.of(merchant).getPlatform() == net.rcetech.md.meta.merchant.Platform.PAYBOX
                ? PAYBOX_CATEGORY_METHODS
                : Map.of();
    }
}
