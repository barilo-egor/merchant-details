package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class MerchantPropertiesServiceTest {

    @Autowired
    private MerchantPropertiesService merchantPropertiesService;

    @ParameterizedTest
    @EnumSource(Merchant.class)
    void getMerchantPropertiesShouldProperties(Merchant merchant) {
        if (Merchant.NOT_ACTIVE.contains(merchant)) {
            return;
        }
        Object merchantProperties = merchantPropertiesService.getProperties(merchant);
        assertNotNull(merchantProperties);
        switch (merchant) {
            case ALFA_TEAM -> assertInstanceOf(AlfaTeamProperties.class, merchantProperties);
            case APPEXBIT -> assertInstanceOf(AppexbitProperties.class, merchantProperties);
            case BIT_ZONE -> assertInstanceOf(BitZoneProperties.class, merchantProperties);
            case CROCO_PAY -> assertInstanceOf(CrocoPayProperties.class, merchantProperties);
            case DAO_PAYMENTS -> assertInstanceOf(DaoPaymentsProperties.class, merchantProperties);
            case EVO_PAY -> assertInstanceOf(EvoPayProperties.class, merchantProperties);
            case EXTASY_PAY -> assertInstanceOf(ExtasyPayProperties.class, merchantProperties);
            case EZE_PAY -> assertInstanceOf(EzePayProperties.class, merchantProperties);
            case FOX_PAYS -> assertInstanceOf(FoxPaysProperties.class, merchantProperties);
            case GEO_TRANSFER -> assertInstanceOf(GeoTransferProperties.class, merchantProperties);
            case HONEY_MONEY -> assertInstanceOf(HoneyMoneyProperties.class, merchantProperties);
            case LUCKY_PAY -> assertInstanceOf(LuckyPayProperties.class, merchantProperties);
            case MOBIUS -> assertInstanceOf(MobiusProperties.class, merchantProperties);
            case NICE_PAY -> assertInstanceOf(NicePayProperties.class, merchantProperties);
            case ONLY_PAYS -> assertInstanceOf(OnlyPaysProperties.class, merchantProperties);
            case ONYX_PAY -> assertInstanceOf(OnyxPayProperties.class, merchantProperties);
            case PANDA_PAY -> assertInstanceOf(PandaPayProperties.class, merchantProperties);
            case PARADOX_PAY -> assertInstanceOf(ParadoxPayProperties.class, merchantProperties);
            case PAY_BOX -> assertInstanceOf(PayBoxProperties.class, merchantProperties);
            case PAY_LEE -> assertInstanceOf(PayLeeProperties.class, merchantProperties);
            case PAY_POINTS -> assertInstanceOf(PayPointsProperties.class, merchantProperties);
            case PAYSCROW -> assertInstanceOf(PayscrowProperties.class, merchantProperties);
            case PSP_WARE -> assertInstanceOf(PspWareProperties.class, merchantProperties);
            case PULSAR -> assertInstanceOf(PulsarProperties.class, merchantProperties);
            case ROSTRAST -> assertInstanceOf(RostrastProperties.class, merchantProperties);
            case WELL_BIT -> assertInstanceOf(WellBitProperties.class, merchantProperties);
            case WORLD_WIDE -> assertInstanceOf(WorldWidePaymentSystemsProperties.class, merchantProperties);
            case WAY_2_PAY -> assertInstanceOf(Way2PayProperties.class, merchantProperties);
            case PAY_CROWN -> assertInstanceOf(PayCrownProperties.class, merchantProperties);
            case PAY_FINITY -> assertInstanceOf(PayFinityProperties.class, merchantProperties);
            default -> throw new IllegalArgumentException();
        }
    }
}