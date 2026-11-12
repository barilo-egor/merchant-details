package net.rcetech.md.integrations.platform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformServiceTest {

    @Mock
    private MerchantServiceRegistry merchantServiceRegistry;

    @Mock
    private MerchantService merchantService;

    private PlatformService platformService;

    @BeforeEach
    void setUp() {
        platformService = new PlatformService(merchantServiceRegistry);
    }

    @Test
    void cancelOrderShouldCancelOrderOnPlatform() {
        CancelOrderRequest request = new CancelOrderRequest();
        request.setOrderId("order-1");
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(merchantService));

        platformService.cancelOrder(Merchant.ALFA_TEAM, request);

        verify(merchantService).cancelOrder(request);
    }

    @Test
    void cancelOrderShouldDoNothingIfPlatformHasNoImplementation() {
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> platformService.cancelOrder(Merchant.ALFA_TEAM, new CancelOrderRequest()));
    }

    @Test
    void sendReceiptShouldSendBytesToPlatform() {
        byte[] content = {1, 2};
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(merchantService));

        platformService.sendReceipt(Merchant.ALFA_TEAM, "order-1", content, "receipt.pdf");

        verify(merchantService).sendReceipt("order-1", content, "receipt.pdf");
    }

    @Test
    void sendReceiptShouldDoNothingIfPlatformHasNoImplementation() {
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> platformService.sendReceipt(Merchant.ALFA_TEAM, "order-1", new byte[0], "receipt.pdf"));
    }
}
