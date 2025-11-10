package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.pulsar.PulsarProperties;
import org.springframework.boot.test.context.SpringBootTest;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        Optional<Object> merchantProperties = merchantPropertiesService.getProperties(merchant);
        assertTrue(merchantProperties.isPresent());
        switch (merchant) {
            case ALFA_TEAM -> assertInstanceOf(AlfaTeamProperties.class, merchantProperties.get());
            case APPEXBIT -> assertInstanceOf(AppexbitProperties.class, merchantProperties.get());
            case BIT_ZONE -> assertInstanceOf(BitZoneProperties.class, merchantProperties.get());
            case CROCO_PAY -> assertInstanceOf(CrocoPayProperties.class, merchantProperties.get());
            case DAO_PAYMENTS -> assertInstanceOf(DaoPaymentsProperties.class, merchantProperties.get());
            case EVO_PAY -> assertInstanceOf(EvoPayProperties.class, merchantProperties.get());
            case EXTASY_PAY -> assertInstanceOf(ExtasyPayProperties.class, merchantProperties.get());
            case FOX_PAYS -> assertInstanceOf(FoxPaysProperties.class, merchantProperties.get());
            case GEO_TRANSFER -> assertInstanceOf(GeoTransferProperties.class, merchantProperties.get());
            case HONEY_MONEY -> assertInstanceOf(HoneyMoneyProperties.class, merchantProperties.get());
            case LUCKY_PAY -> assertInstanceOf(LuckyPayProperties.class, merchantProperties.get());
            case MOBIUS -> assertInstanceOf(MobiusProperties.class, merchantProperties.get());
            case NICE_PAY -> assertInstanceOf(NicePayProperties.class, merchantProperties.get());
            case ONLY_PAYS -> assertInstanceOf(OnlyPaysProperties.class, merchantProperties.get());
            case ONYX_PAY -> assertInstanceOf(OnyxPayProperties.class, merchantProperties.get());
            case ONYX_PAY_SIM -> assertInstanceOf(OnyxPaySimProperties.class, merchantProperties.get());
            case YA_PAY -> assertInstanceOf(YaPayProperties.class, merchantProperties.get());
            case PAY_LEE -> assertInstanceOf(PayLeeProperties.class, merchantProperties.get());
            case PAYSCROW -> assertInstanceOf(PayscrowPropertiesImpl.class, merchantProperties.get());
            case PSP_WARE -> assertInstanceOf(PspWareProperties.class, merchantProperties.get());
            case PULSAR -> assertInstanceOf(PulsarProperties.class, merchantProperties.get());
            case ROSTRAST -> assertInstanceOf(RostrastProperties.class, merchantProperties.get());
            case WELL_BIT -> assertInstanceOf(WellBitProperties.class, merchantProperties.get());
            case PAY_CROWN -> assertInstanceOf(PayCrownProperties.class, merchantProperties.get());
            case STORM_TRADE -> assertInstanceOf(StormTradeProperties.class, merchantProperties.get());
            default -> throw new IllegalArgumentException();
        }
    }

    @Test
    void getMerchantPropertiesShouldReturnEmptyOptional() {
        assertTrue(merchantPropertiesService.getProperties(Merchant.ALFA_TEAM_ALFA).isEmpty());
    }
}