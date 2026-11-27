package net.rcetech.md.integrations.platform;

import org.junit.jupiter.api.Test;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MerchantServiceRegistryTest {

    private static MerchantService service(Merchant merchant) {
        MerchantService service = mock(MerchantService.class);
        when(service.getMerchant()).thenReturn(merchant);
        return service;
    }

    @SuppressWarnings("unchecked")
    private static MerchantOrderCreationService<?, ?> orderCreationService() {
        return mock(MerchantOrderCreationService.class);
    }

    @Test
    void ownServiceBeanShouldTakePrecedenceOverFactory() {
        MerchantService own = service(Merchant.SMACK_PAY);
        MerchantServiceFactory factory = mock(MerchantServiceFactory.class);

        MerchantServiceRegistry registry = new MerchantServiceRegistry(List.of(own), factory);

        assertEquals(Optional.of(own), registry.getService(Merchant.SMACK_PAY));
        verify(factory, never()).create(Merchant.SMACK_PAY);
    }

    @Test
    void factoryShouldCreateServiceForMerchantWithoutOwnBean() {
        MerchantServiceFactory factory = mock(MerchantServiceFactory.class);
        MerchantOrderCreationService<?, ?> fromFactory = orderCreationService();
        doReturn(Optional.of(fromFactory)).when(factory).create(Merchant.CANPAY);

        MerchantServiceRegistry registry = new MerchantServiceRegistry(List.of(), factory);

        assertEquals(Optional.of(fromFactory), registry.getService(Merchant.CANPAY));
    }

    @Test
    void unconfiguredMerchantShouldNotFailStartup() {
        MerchantServiceFactory factory = mock(MerchantServiceFactory.class);
        doThrow(new IllegalStateException("В конфиге нет группы 'alfa-team-white-triangle'"))
                .when(factory).create(any(Merchant.class));

        MerchantServiceRegistry registry = assertDoesNotThrow(
                () -> new MerchantServiceRegistry(List.of(), factory));

        assertEquals(Optional.empty(), registry.getService(Merchant.ALFA_TEAM_WT));
    }

    @Test
    void otherMerchantsShouldBeCreatedDespiteOneUnconfigured() {
        MerchantServiceFactory factory = mock(MerchantServiceFactory.class);
        MerchantOrderCreationService<?, ?> configured = orderCreationService();
        doThrow(new IllegalStateException("В конфиге нет группы 'alfa-team-white-triangle'"))
                .when(factory).create(Merchant.ALFA_TEAM_WT);
        doReturn(Optional.of(configured)).when(factory).create(Merchant.CANPAY);

        MerchantServiceRegistry registry = new MerchantServiceRegistry(List.of(), factory);

        assertEquals(Optional.empty(), registry.getService(Merchant.ALFA_TEAM_WT));
        assertEquals(Optional.of(configured), registry.getService(Merchant.CANPAY));
    }
}
