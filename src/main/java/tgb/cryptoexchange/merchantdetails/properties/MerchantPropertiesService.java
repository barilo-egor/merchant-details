package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.stereotype.Service;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static tgb.cryptoexchange.merchantdetails.enums.Merchant.*;

/**
 * Класс создан на время трансфера сервиса из монолита
 */
@Service
public class MerchantPropertiesService {

    private final Map<Merchant, Object> properties;

    public MerchantPropertiesService(AlfaTeamProperties alfaTeamProperties,
                                     BitZoneProperties bitZoneProperties,
                                     CrocoPayProperties crocoPayProperties,
                                     EvoPayProperties evoPayProperties,
                                     ExtasyPayProperties extasyPayProperties,
                                     FoxPaysProperties foxPaysProperties,
                                     GeoTransferProperties geoTransferProperties,
                                     LuckyPayProperties luckyPayProperties,
                                     MobiusProperties mobiusProperties,
                                     NicePayProperties nicePayProperties,
                                     OnlyPaysProperties onlyPaysProperties,
                                     OnyxPayProperties onyxPayProperties,
                                     YaPayProperties yaPayProperties,
                                     PayLeeProperties payLeeProperties,
                                     PayscrowPropertiesImpl payscrowPropertiesImpl,
                                     PspWareProperties pspWareProperties,
                                     RostrastProperties rostrastProperties,
                                     WellBitProperties wellBitProperties,
                                     StormTradeProperties stormTradeProperties) {
        properties = new EnumMap<>(Merchant.class);
        properties.put(ALFA_TEAM, alfaTeamProperties);
        properties.put(BIT_ZONE, bitZoneProperties);
        properties.put(CROCO_PAY, crocoPayProperties);
        properties.put(EVO_PAY, evoPayProperties);
        properties.put(EXTASY_PAY, extasyPayProperties);
        properties.put(FOX_PAYS, foxPaysProperties);
        properties.put(GEO_TRANSFER, geoTransferProperties);
        properties.put(LUCKY_PAY, luckyPayProperties);
        properties.put(MOBIUS, mobiusProperties);
        properties.put(NICE_PAY, nicePayProperties);
        properties.put(ONLY_PAYS, onlyPaysProperties);
        properties.put(ONYX_PAY, onyxPayProperties);
        properties.put(YA_PAY, yaPayProperties);
        properties.put(PAY_LEE, payLeeProperties);
        properties.put(PAYSCROW, payscrowPropertiesImpl);
        properties.put(PSP_WARE, pspWareProperties);
        properties.put(ROSTRAST, rostrastProperties);
        properties.put(WELL_BIT, wellBitProperties);
        properties.put(STORM_TRADE, stormTradeProperties);
    }

    public Optional<Object> getProperties(Merchant merchant) {
        Object merchantProperties = properties.get(merchant);
        return Optional.ofNullable(merchantProperties);
    }
}
