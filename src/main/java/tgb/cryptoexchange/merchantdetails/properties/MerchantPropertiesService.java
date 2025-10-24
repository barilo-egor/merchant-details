package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.stereotype.Service;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

import java.util.EnumMap;
import java.util.Map;

import static tgb.cryptoexchange.merchantdetails.enums.Merchant.*;

/**
 * Класс создан на время трансфера сервиса из монолита
 */
@Service
public class MerchantPropertiesService {

    private final Map<Merchant, Object> properties;

    public MerchantPropertiesService(AppexbitProperties appexbitProperties,
                                     BitZoneProperties bitZoneProperties,
                                     CrocoPayProperties crocoPayProperties,
                                     DaoPaymentsProperties daoPaymentsProperties,
                                     EvoPayProperties evoPayProperties,
                                     ExtasyPayProperties extasyPayProperties,
                                     EzePayProperties ezePayProperties,
                                     FoxPaysProperties foxPaysProperties,
                                     GeoTransferProperties geoTransferProperties,
                                     HoneyMoneyProperties honeyMoneyProperties,
                                     LuckyPayProperties luckyPayProperties,
                                     MobiusProperties mobiusProperties,
                                     NicePayProperties nicePayProperties,
                                     OnlyPaysProperties onlyPaysProperties,
                                     OnyxPayProperties onyxPayProperties,
                                     PandaPayProperties pandaPayProperties,
                                     ParadoxPayProperties paradoxPayProperties,
                                     PayBoxProperties payBoxProperties,
                                     PayLeeProperties payLeeProperties,
                                     PayPointsProperties payPointsProperties,
                                     PayscrowProperties payscrowProperties,
                                     PspWareProperties pspWareProperties,
                                     PulsarProperties pulsarProperties,
                                     RostrastProperties rostrastProperties,
                                     WellBitProperties wellBitProperties,
                                     WorldWidePaymentSystemsProperties worldWidePaymentSystemsProperties) {
        properties = new EnumMap<>(Merchant.class);
        properties.put(APPEXBIT, appexbitProperties);
        properties.put(BIT_ZONE, bitZoneProperties);
        properties.put(CROCO_PAY, crocoPayProperties);
        properties.put(DAO_PAYMENTS, daoPaymentsProperties);
        properties.put(EVO_PAY, evoPayProperties);
        properties.put(EXTASY_PAY, extasyPayProperties);
        properties.put(EZE_PAY, ezePayProperties);
        properties.put(FOX_PAYS, foxPaysProperties);
        properties.put(GEO_TRANSFER, geoTransferProperties);
        properties.put(HONEY_MONEY, honeyMoneyProperties);
        properties.put(LUCKY_PAY, luckyPayProperties);
        properties.put(MOBIUS, mobiusProperties);
        properties.put(NICE_PAY, nicePayProperties);
        properties.put(ONLY_PAYS, onlyPaysProperties);
        properties.put(ONYX_PAY, onyxPayProperties);
        properties.put(PANDA_PAY, pandaPayProperties);
        properties.put(PARADOX_PAY, paradoxPayProperties);
        properties.put(PAY_BOX, payBoxProperties);
        properties.put(PAY_LEE, payLeeProperties);
        properties.put(PAY_POINTS, payPointsProperties);
        properties.put(PAYSCROW, payscrowProperties);
        properties.put(PSP_WARE, pspWareProperties);
        properties.put(PULSAR, pulsarProperties);
        properties.put(ROSTRAST, rostrastProperties);
        properties.put(WELL_BIT, wellBitProperties);
        properties.put(WORLD_WIDE, worldWidePaymentSystemsProperties);
    }

    public Object getProperties(Merchant merchant) {
        return properties.get(merchant);
    }
}
