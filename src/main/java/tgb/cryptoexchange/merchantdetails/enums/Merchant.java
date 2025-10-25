package tgb.cryptoexchange.merchantdetails.enums;

import java.util.List;

/**
 * Перечисление мерчантов
 */
public enum Merchant {
    ALFA_TEAM,
    PAY_POINTS,
    ONLY_PAYS,
    EVO_PAY,
    NICE_PAY,
    HONEY_MONEY,
    WELL_BIT,
    CROCO_PAY,
    EZE_PAY,
    BIT_ZONE,
    PANDA_PAY,
    ONYX_PAY,
    EXTASY_PAY,
    PAY_LEE,
    PULSAR,
    PSP_WARE,
    WORLD_WIDE,
    WAY_2_PAY,
    PAY_CROWN,
    PAYSCROW,
    LUCKY_PAY,
    APPEXBIT,
    MOBIUS,
    PAY_BOX,
    PARADOX_PAY,
    DAO_PAYMENTS,
    GEO_TRANSFER,
    ROSTRAST,
    FOX_PAYS,

    /**
     * Более не используются
     */
    ALFA_TEAM_TJS,
    ALFA_TEAM_VTB,
    ALFA_TEAM_ALFA,
    ALFA_TEAM_SBER,
    PAY_FINITY,
    NOROS,
    EASY_PAY,
    DASH_PAY;

    public static final List<Merchant> NOT_ACTIVE = List.of(
            ALFA_TEAM_ALFA,
            ALFA_TEAM_VTB,
            ALFA_TEAM_TJS,
            ALFA_TEAM_SBER,
            NOROS,
            EASY_PAY,
            DASH_PAY
    );
}
