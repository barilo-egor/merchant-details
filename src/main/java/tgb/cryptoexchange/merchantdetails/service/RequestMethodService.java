package tgb.cryptoexchange.merchantdetails.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.constants.MerchantConstants;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для работы с категориями мерчантов({@link RequestMethod}).
 */
@Service
public class RequestMethodService {

    private final Map<RequestMethod, Map<Merchant, MerchantMethod>> merchantMethods = new EnumMap<>(RequestMethod.class);

    private final Map<RequestMethod, Set<Merchant>> merchants = new EnumMap<>(RequestMethod.class);

    @PostConstruct
    public void init() {
        for (MerchantConstants merchantConstant : MerchantConstants.values()) {
            for (MerchantMethod merchantMethod : merchantConstant.getMethods()) {
                if (merchantMethod.getRequestMethod().isPresent()) {
                    merchantMethods.computeIfAbsent(
                                    merchantMethod.getRequestMethod().get(),
                                    m -> new EnumMap<>(Merchant.class)
                            )
                            .put(Merchant.valueOf(merchantConstant.name()), merchantMethod);
                }
            }
        }
        for (Map.Entry<RequestMethod, Map<Merchant, MerchantMethod>> entry : merchantMethods.entrySet()) {
            merchants.put(entry.getKey(), entry.getValue().keySet());
        }
    }

    /**
     * Получение списка названий методов мерчанта по запрошенным категориям.
     * @param merchant мерчант, методы которого необходимо получить
     * @param requestMethods запрашиваемые категории
     * @return список названий({@link MerchantMethod#name()}) методов мерчантов
     */
    public List<String> getMerchantMethods(Merchant merchant, List<RequestMethod> requestMethods) {
        List<String> methods = new ArrayList<>();
        for (RequestMethod requestMethod : requestMethods) {
            methods.add(merchantMethods.get(requestMethod).get(merchant).name());
        }
        return methods;
    }

    /**
     * Получение множества мерчантов, у которых есть методы по запрашиваемым категориям.
     * @param requestMethods запрашиваемые категории
     * @return множество мерчантов
     */
    public Set<Merchant> getMerchants(List<RequestMethod> requestMethods) {
        return requestMethods.stream()
                .flatMap(method -> merchants.get(method).stream())
                .collect(Collectors.toSet());
    }
}
