package net.rcetech.md.integrations.platform.bridgepay;

import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.ExtasyPayQrVietProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtasyPayQrVietMerchantOrderCreationServiceTest {

    @Mock
    private ExtasyPayQrVietProperties extasyPayQrVietProperties;

    @Mock
    private CallbackConfig callbackConfig;

    @InjectMocks
    private ExtasyPayQrVietMerchantOrderCreationService extasyPayQrVietMerchantOrderCreationService;

    @Test
    void getMerchantShouldReturnExtasyPayQrViet() {
        assertEquals(Merchant.EXTASY_PAY_QR_VIET, extasyPayQrVietMerchantOrderCreationService.getMerchant());
    }

    @Test
    void bodyShouldBuildRequestObjectWithMerchantInNotificationUrl() {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(1000);
        detailsRequest.setMethod(Method.VIET_QR.name());
        when(callbackConfig.getGatewayUrl()).thenReturn("https://gateway.example.com");
        when(callbackConfig.getCallbackSecret()).thenReturn("secret");
        when(extasyPayQrVietProperties.token()).thenReturn("token");

        Request actual = extasyPayQrVietMerchantOrderCreationService.body(detailsRequest);

        assertEquals("https://gateway.example.com/merchant-details/callback?merchant=EXTASY_PAY_QR_VIET&secret=secret",
                actual.getNotificationUrl());
    }

    @Test
    void buildResponseShouldSetQrForVietQrDeal() {
        Response response = new Response();
        DealDTO dealDTO = new DealDTO();
        dealDTO.setPaymentOption(Method.VIET_QR);
        dealDTO.setQrCodeLink("https://qr.example.com/viet/9e6eee50");
        response.setDeals(List.of(dealDTO));
        response.setId("7e6eee50-dcbb-47be-85a5-494db6d49157");

        Optional<DetailsResponse> actual = extasyPayQrVietMerchantOrderCreationService.buildResponse(response);

        assertTrue(actual.isPresent());
        DetailsResponse actualResponse = actual.get();
        assertAll(
                () -> assertEquals(Merchant.EXTASY_PAY_QR_VIET, actualResponse.getMerchant()),
                () -> assertEquals("https://qr.example.com/viet/9e6eee50", actualResponse.getQr()),
                () -> assertEquals("VIET_QR", actualResponse.getPaymentMethod())
        );
    }
}
