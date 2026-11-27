package net.rcetech.md.integrations.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.platform.MerchantMethod;
import net.rcetech.md.integrations.platform.PlatformRegistry;
import net.rcetech.md.meta.detailsapi.enums.RequestMethod;
import net.rcetech.md.meta.exception.MerchantMethodNotFoundException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для работы с категориями мерчантов({@link RequestMethod}).
 * Данные берутся из {@link PlatformRegistry} (справочник + методы площадок);
 * у площадки с несколькими методами одной категории выбор задан явно —
 * порядок объявления констант enum'а на результат не влияет.
 */
@Service
public class RequestMethodService {

    private final Map<RequestMethod, Map<Merchant, MerchantMethod>> merchantMethods = new EnumMap<>(RequestMethod.class);

    private final Map<RequestMethod, Set<Merchant>> merchants = new EnumMap<>(RequestMethod.class);

    private final PlatformRegistry platformRegistry;

    public RequestMethodService(PlatformRegistry platformRegistry) {
        this.platformRegistry = platformRegistry;
    }

    @PostConstruct
    public void init() {
        for (Merchant merchant : Merchant.values()) {
            Map<RequestMethod, String> explicit = platformRegistry.explicitCategoryMethods(merchant);
            Map<RequestMethod, List<String>> duplicatesWithoutExplicit = new EnumMap<>(RequestMethod.class);
            for (MerchantMethod merchantMethod : platformRegistry.methods(merchant)) {
                Optional<RequestMethod> requestMethod = merchantMethod.getRequestMethod();
                if (requestMethod.isEmpty()) {
                    continue;
                }
                RequestMethod category = requestMethod.get();
                Map<Merchant, MerchantMethod> byMerchant = merchantMethods.computeIfAbsent(
                        category, m -> new EnumMap<>(Merchant.class));
                if (merchantMethod.name().equals(explicit.get(category))) {
                    // Явное соответствие из реестра перекрывает любой порядок объявления.
                    byMerchant.put(merchant, merchantMethod);
                } else if (byMerchant.containsKey(merchant)) {
                    duplicatesWithoutExplicit.computeIfAbsent(category, c -> new ArrayList<>())
                            .add(merchantMethod.name());
                } else {
                    byMerchant.put(merchant, merchantMethod);
                }
            }
            if (!duplicatesWithoutExplicit.isEmpty()) {
                throw new IllegalStateException(
                        "У мерчанта %s несколько методов одной категории без явного соответствия "
                                + "(задайте explicitCategoryMethods в PlatformRegistry): %s".formatted(
                                merchant, duplicatesWithoutExplicit));
            }
        }
        for (Map.Entry<RequestMethod, Map<Merchant, MerchantMethod>> entry : merchantMethods.entrySet()) {
            merchants.put(entry.getKey(), entry.getValue().keySet());
        }
    }

    /**
     * Получение списка названий методов мерчанта по запрошенным категориям.
     *
     * @param merchant       мерчант, методы которого необходимо получить
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
     * Получение названия метода мерчанта по запрошенной категории.
     *
     * @param merchant      мерчант, метод которого необходимо получить
     * @param requestMethod запрашиваемая категория
     * @return название({@link MerchantMethod#name()}) метода мерчанта
     * @throws MerchantMethodNotFoundException если метод для мерчанта не найден
     */
    public String getMerchantMethod(Merchant merchant, RequestMethod requestMethod) {
        return Optional.ofNullable(merchantMethods.get(requestMethod))
                .map(methods -> methods.get(merchant))
                .map(MerchantMethod::name)
                .orElseThrow(() -> new MerchantMethodNotFoundException(
                        "Method not found for merchant " + merchant + " and category " + requestMethod
                ));
    }

    /**
     * Получение множества мерчантов, у которых есть методы по запрашиваемым категориям.
     *
     * @param requestMethods запрашиваемые категории
     * @return множество мерчантов
     */
    public Set<Merchant> getMerchants(List<RequestMethod> requestMethods) {
        return requestMethods.stream()
                .flatMap(method -> merchants.get(method).stream())
                .collect(Collectors.toSet());
    }
}
