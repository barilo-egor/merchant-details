package tgb.cryptoexchange.merchantdetails.constants;

import lombok.AllArgsConstructor;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;
import tgb.cryptoexchange.merchantdetails.details.bridgepay.Method;
import tgb.cryptoexchange.merchantdetails.details.bridgepay.Status;

import java.util.Arrays;
import java.util.List;

/**
 * Перечисление мерчантов
 */
@AllArgsConstructor
public enum Merchant {
    ALFA_TEAM(
            Status.values(),
            Method.values()
    ),
    ONLY_PAYS(
            tgb.cryptoexchange.merchantdetails.details.onlypays.Status.values(),
            tgb.cryptoexchange.merchantdetails.details.onlypays.Method.values()
    ),
    EVO_PAY(
            tgb.cryptoexchange.merchantdetails.details.evopay.Status.values(),
            tgb.cryptoexchange.merchantdetails.details.evopay.Method.values()
    ),
    NICE_PAY(
            tgb.cryptoexchange.merchantdetails.details.nicepay.Status.values(),
            tgb.cryptoexchange.merchantdetails.details.nicepay.Method.values()
    ),
    HONEY_MONEY(
            tgb.cryptoexchange.merchantdetails.details.honeymoney.Status.values(),
            tgb.cryptoexchange.merchantdetails.details.honeymoney.Method.values()
    ),
    WELL_BIT(
            tgb.cryptoexchange.merchantdetails.details.wellbit.Status.values(),
            tgb.cryptoexchange.merchantdetails.details.wellbit.Method.values()
    ),
    CROCO_PAY(
            tgb.cryptoexchange.merchantdetails.details.crocopay.Status.values(),
            tgb.cryptoexchange.merchantdetails.details.crocopay.Method.values()
    ),
    BIT_ZONE(
            tgb.cryptoexchange.merchantdetails.details.bitzone.Status.values(),
            tgb.cryptoexchange.merchantdetails.details.bitzone.Method.values()
    ),
    ONYX_PAY(
            Status.values(),
            Method.values()
    ),
    EXTASY_PAY(
            tgb.cryptoexchange.merchantdetails.details.paybox.Status.values(),
            tgb.cryptoexchange.merchantdetails.details.paybox.Method.values()
    ),
    PAY_LEE(
            tgb.cryptoexchange.merchantdetails.details.paylee.Status.values(),
            tgb.cryptoexchange.merchantdetails.details.paylee.Method.values()
    ),
    PSP_WARE(
            tgb.cryptoexchange.merchantdetails.details.pspware.Status.values(),
            tgb.cryptoexchange.merchantdetails.details.pspware.Method.values()
    ),
    PAY_CROWN(
            tgb.cryptoexchange.merchantdetails.details.paycrown.Status.values(),
            tgb.cryptoexchange.merchantdetails.details.paycrown.Method.values()
    ),
    PAYSCROW(
            tgb.cryptoexchange.merchantdetails.details.payscrow.Status.values(),
            tgb.cryptoexchange.merchantdetails.details.payscrow.Method.values()
    ),
    LUCKY_PAY(
            tgb.cryptoexchange.merchantdetails.details.payscrow.Status.values(),
            tgb.cryptoexchange.merchantdetails.details.payscrow.Method.values()
    ),
    APPEXBIT(
            tgb.cryptoexchange.merchantdetails.details.appexbit.Status.values(),
            tgb.cryptoexchange.merchantdetails.details.appexbit.Method.values()
    ),
    MOBIUS(
            tgb.cryptoexchange.merchantdetails.details.levelpay.Status.values(),
            tgb.cryptoexchange.merchantdetails.details.levelpay.Method.values()
    ),
    YA_PAY(
            tgb.cryptoexchange.merchantdetails.details.paybox.Status.values(),
            tgb.cryptoexchange.merchantdetails.details.paybox.Method.values()
    ),
    DAO_PAYMENTS(
            tgb.cryptoexchange.merchantdetails.details.daopayments.Status.values(),
            tgb.cryptoexchange.merchantdetails.details.daopayments.Method.values()
    ),
    GEO_TRANSFER(
            Status.values(),
            Method.values()
    ),
    ROSTRAST(
            Status.values(),
            Method.values()
    ),
    FOX_PAYS(
            tgb.cryptoexchange.merchantdetails.details.levelpay.Status.values(),
            tgb.cryptoexchange.merchantdetails.details.levelpay.Method.values()
    ),
    STORM_TRADE(
            Status.values(),
            Method.values()
    ),
    SETTLE_X(
            tgb.cryptoexchange.merchantdetails.details.settlex.Status.values(),
            tgb.cryptoexchange.merchantdetails.details.settlex.Method.values()
    ),;

    private final MerchantOrderStatus[] statuses;

    private final MerchantMethod[] methods;

    public List<MerchantOrderStatus> getStatuses() {
        return Arrays.asList(statuses);
    }

    public List<MerchantMethod> getMethods() {
        return Arrays.asList(methods);
    }
}
