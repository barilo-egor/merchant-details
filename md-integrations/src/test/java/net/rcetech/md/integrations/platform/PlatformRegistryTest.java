package net.rcetech.md.integrations.platform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import net.rcetech.md.meta.detailsapi.enums.RequestMethod;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PlatformRegistryTest {

    private final PlatformRegistry registry = new PlatformRegistry();

    @ParameterizedTest(name = "[{0}] statuses not empty")
    @EnumSource(Merchant.class)
    void statusesShouldNotBeEmpty(Merchant merchant) {
        assertDoesNotThrow(() -> registry.statuses(merchant));
        assertFalse(registry.statuses(merchant).isEmpty());
    }

    @ParameterizedTest(name = "[{0}] methods not empty")
    @EnumSource(Merchant.class)
    void methodsShouldNotBeEmpty(Merchant merchant) {
        assertDoesNotThrow(() -> registry.methods(merchant));
        assertFalse(registry.methods(merchant).isEmpty());
    }

    @Test
    void extasyMethodsShouldBeSubsetFromCatalog() {
        assertEquals(List.of("SBP", "TO_CARD"), names(Merchant.EXTASY_PAY));
        assertEquals(List.of("SBP_QR"), names(Merchant.EXTASY_PAY_QR_NSPK));
        assertEquals(List.of("VIET_QR"), names(Merchant.EXTASY_PAY_QR_VIET));
    }

    @Test
    void gambitAndHesoyamHaveDifferentMethodSets() {
        assertNotEquals(names(Merchant.GAMBIT), names(Merchant.HESOYAM));
    }

    @Test
    void payboxCategoryMethodsShouldBeExplicit() {
        Map<RequestMethod, String> explicit = registry.explicitCategoryMethods(Merchant.PW_PAY);
        assertEquals("CARD_RECEIPT", explicit.get(RequestMethod.CARD));
        assertEquals("SBP_RECEIPT", explicit.get(RequestMethod.SBP));
        // Явный выбор входит в набор методов мерчанта.
        List<String> names = names(Merchant.PW_PAY);
        assertTrue(names.containsAll(explicit.values()));
    }

    @Test
    void everyDuplicateCategoryShouldHaveExplicitChoice() {
        // Инвариант порядка: у площадки с несколькими методами одной категории
        // явный выбор обязан существовать (иначе RequestMethodService падает на старте).
        for (Merchant merchant : Merchant.values()) {
            Map<RequestMethod, List<String>> byCategory = registry.methods(merchant).stream()
                    .filter(m -> m.getRequestMethod().isPresent())
                    .collect(Collectors.groupingBy(m -> m.getRequestMethod().get(),
                            Collectors.mapping(MerchantMethod::name, Collectors.toList())));
            Map<RequestMethod, String> explicit = registry.explicitCategoryMethods(merchant);
            for (Map.Entry<RequestMethod, List<String>> entry : byCategory.entrySet()) {
                if (entry.getValue().size() > 1) {
                    assertTrue(explicit.containsKey(entry.getKey()),
                            "Мерчант %s: категория %s имеет несколько методов %s без явного выбора"
                                    .formatted(merchant, entry.getKey(), entry.getValue()));
                    assertTrue(entry.getValue().contains(explicit.get(entry.getKey())),
                            "Мерчант %s: явный метод %s не входит в набор площадки".formatted(
                                    merchant, explicit.get(entry.getKey())));
                }
            }
        }
    }

    private List<String> names(Merchant merchant) {
        return registry.methods(merchant).stream().map(MerchantMethod::name).toList();
    }
}
