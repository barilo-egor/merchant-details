package net.rcetech.md.domain.repository;

import net.rcetech.md.domain.AbstractDataJpaTest;
import net.rcetech.md.domain.entity.AutoConfirmConfig;
import net.rcetech.md.domain.entity.MerchantConfig;
import net.rcetech.md.domain.entity.MerchantSuccessStatus;
import net.rcetech.md.meta.constants.AutoConfirmType;
import net.rcetech.md.meta.dto.MerchantConfigFilter;
import net.rcetech.md.meta.projection.AutoConfirmConfigProjection;
import net.rcetech.md.meta.projection.MerchantConfigProjection;
import net.rcetech.md.meta.projection.MerchantSuccessStatusProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import tgb.cryptoexchange.commons.enums.CryptoCurrency;
import tgb.cryptoexchange.commons.enums.DeliveryType;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MerchantConfigSpecificationsTest extends AbstractDataJpaTest {

    @Autowired
    private MerchantConfigRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        repository.save(config(Merchant.ALFA_TEAM, 1));
        repository.save(config(Merchant.SETTLE_X, 2));
        repository.save(config(Merchant.BIT_ZONE, 3));
    }

    @Test
    @DisplayName("Без фильтра должны вернуться все конфиги.")
    void matchesShouldReturnAllConfigsIfFilterIsNull() {
        assertEquals(List.of(Merchant.ALFA_TEAM, Merchant.SETTLE_X, Merchant.BIT_ZONE), merchants(find(null)));
    }

    @Test
    @DisplayName("Пустой фильтр не должен ограничивать выборку.")
    void matchesShouldReturnAllConfigsIfFilterIsEmpty() {
        assertEquals(List.of(Merchant.ALFA_TEAM, Merchant.SETTLE_X, Merchant.BIT_ZONE),
                merchants(find(new MerchantConfigFilter(List.of(), null))));
    }

    @Test
    @DisplayName("Должны вернуться только конфиги мерчантов из фильтра.")
    void matchesShouldFilterByMerchants() {
        assertEquals(List.of(Merchant.ALFA_TEAM, Merchant.BIT_ZONE),
                merchants(find(new MerchantConfigFilter(List.of(Merchant.BIT_ZONE, Merchant.ALFA_TEAM), null))));
    }

    @Test
    @DisplayName("Должен вернуться только конфиг с порядком из фильтра.")
    void matchesShouldFilterByMerchantOrder() {
        assertEquals(List.of(Merchant.SETTLE_X), merchants(find(new MerchantConfigFilter(null, 2))));
    }

    @Test
    @DisplayName("Условия фильтра должны объединяться через И.")
    void matchesShouldCombineConditions() {
        assertEquals(List.of(), merchants(find(new MerchantConfigFilter(List.of(Merchant.ALFA_TEAM), 2))));
    }

    @Test
    @DisplayName("Проекция должна содержать успешные статусы и настройки автоподтверждения.")
    void projectionShouldContainNestedCollections() {
        MerchantConfigProjection projection = find(new MerchantConfigFilter(List.of(Merchant.SETTLE_X), null))
                .getContent().getFirst();

        AutoConfirmConfigProjection confirmConfig = projection.getConfirmConfigs().getFirst();
        assertAll(
                () -> assertEquals(2, projection.getMerchantOrder()),
                () -> assertEquals(List.of("SUCCESS", "COMPLETED"), projection.getSuccessStatuses().stream()
                        .map(MerchantSuccessStatusProjection::getStatus)
                        .toList()),
                () -> assertEquals(1, projection.getConfirmConfigs().size()),
                () -> assertEquals(CryptoCurrency.BITCOIN, confirmConfig.getCryptoCurrency()),
                () -> assertEquals(AutoConfirmType.AUTO_WITHDRAWAL, confirmConfig.getAutoConfirmType()),
                () -> assertEquals(DeliveryType.VIP, confirmConfig.getDeliveryType())
        );
    }

    private Page<MerchantConfigProjection> find(MerchantConfigFilter filter) {
        return repository.findBy(MerchantConfigSpecifications.matches(filter), query -> query
                .as(MerchantConfigProjection.class)
                .page(PageRequest.of(0, 10, Sort.by("merchantOrder"))));
    }

    private static List<Merchant> merchants(Page<MerchantConfigProjection> page) {
        return page.getContent().stream().map(MerchantConfigProjection::getMerchant).toList();
    }

    private static MerchantConfig config(Merchant merchant, int order) {
        List<MerchantSuccessStatus> successStatuses = new ArrayList<>();
        successStatuses.add(MerchantSuccessStatus.builder().status("SUCCESS").build());
        successStatuses.add(MerchantSuccessStatus.builder().status("COMPLETED").build());
        List<AutoConfirmConfig> confirmConfigs = new ArrayList<>();
        confirmConfigs.add(AutoConfirmConfig.builder()
                .cryptoCurrency(CryptoCurrency.BITCOIN)
                .autoConfirmType(AutoConfirmType.AUTO_WITHDRAWAL)
                .deliveryType(DeliveryType.VIP)
                .build());
        return MerchantConfig.builder()
                .merchant(merchant)
                .merchantOrder(order)
                .isOn(true)
                .minAmount(1)
                .maxAmount(5000)
                .minDealsCount(0)
                .successStatuses(successStatuses)
                .confirmConfigs(confirmConfigs)
                .build();
    }
}
