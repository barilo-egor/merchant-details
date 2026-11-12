package net.rcetech.md.app.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.app.AbstractMySqlTest;
import net.rcetech.md.meta.dto.ApiMerchantConfigDTO;
import net.rcetech.md.meta.dto.UpdateApiMerchantConfigDTO;
import net.rcetech.md.domain.repository.ApiMerchantConfigRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class ApiMerchantConfigServiceIntegrationTest extends AbstractMySqlTest {

    @Autowired
    private ApiMerchantConfigService service;

    @Autowired
    private ApiMerchantConfigRepository repository;

    @Test
    void findAllAndCreateIfNotExistShouldCreateConfigsOnceAndReturnThemOnNextCall() {
        UUID ownerId = UUID.randomUUID();

        List<ApiMerchantConfigDTO> created = service.findAllAndCreateIfNotExist(ownerId);
        List<ApiMerchantConfigDTO> loaded = service.findAllAndCreateIfNotExist(ownerId);

        ApiMerchantConfigDTO first = created.getFirst();
        assertAll(
                () -> assertEquals(Merchant.values().length, created.size()),
                () -> assertEquals(created, loaded),
                () -> assertNotNull(first.id()),
                () -> assertEquals(Boolean.FALSE, first.isOn()),
                () -> assertEquals(1, first.minAmount()),
                () -> assertEquals(5000, first.maxAmount()),
                () -> assertEquals(1, first.merchantOrder())
        );
    }

    @Test
    void updateShouldReturnUpdatedValues() {
        ApiMerchantConfigDTO target = service.findAllAndCreateIfNotExist(UUID.randomUUID()).getFirst();
        UpdateApiMerchantConfigDTO dto = new UpdateApiMerchantConfigDTO();
        dto.setId(target.id());
        dto.setIsOn(true);
        dto.setMinAmount(100);
        dto.setMaxAmount(777);
        dto.setNewOrder(3);
        dto.setSwap(false);

        ApiMerchantConfigDTO updated = service.update(dto);

        assertEquals(new ApiMerchantConfigDTO(target.id(), true, target.merchant(), 777, 100, 3), updated);
        assertEquals(updated, repository.findDtoById(target.id()).orElseThrow());
    }

    @Test
    void updateShouldSwapOrderWithConfigOnNewOrder() {
        List<ApiMerchantConfigDTO> configs = service.findAllAndCreateIfNotExist(UUID.randomUUID());
        ApiMerchantConfigDTO target = configs.get(0);
        ApiMerchantConfigDTO other = configs.get(4);
        UpdateApiMerchantConfigDTO dto = new UpdateApiMerchantConfigDTO();
        dto.setId(target.id());
        dto.setNewOrder(other.merchantOrder());
        dto.setSwap(true);

        ApiMerchantConfigDTO updated = service.update(dto);

        assertEquals(other.merchantOrder(), updated.merchantOrder());
        assertEquals(target.merchantOrder(), repository.findDtoById(other.id()).orElseThrow().merchantOrder());
    }
}
