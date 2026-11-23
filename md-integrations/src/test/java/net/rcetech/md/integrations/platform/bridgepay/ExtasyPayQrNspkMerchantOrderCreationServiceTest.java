package net.rcetech.md.integrations.platform.bridgepay;

import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.ExtasyPayQrNspkProperties;
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
class ExtasyPayQrNspkMerchantOrderCreationServiceTest {

    @Mock
    private ExtasyPayQrNspkProperties extasyPayQrNspkProperties;

    @Mock
    private CallbackConfig callbackConfig;

    @InjectMocks
    private ExtasyPayQrNspkMerchantOrderCreationService extasyPayQrNspkMerchantOrderCreationService;

    @Test
    void getMerchantShouldReturnExtasyPayQrNspk() {
        assertEquals(Merchant.EXTASY_PAY_QR_NSPK, extasyPayQrNspkMerchantOrderCreationService.getMerchant());
    }

    @Test
    void bodyShouldBuildRequestObjectWithMerchantInNotificationUrl() {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(1000);
        detailsRequest.setMethod(Method.SBP_QR.name());
        when(callbackConfig.getGatewayUrl()).thenReturn("https://gateway.example.com");
        when(callbackConfig.getCallbackSecret()).thenReturn("secret");
        when(extasyPayQrNspkProperties.token()).thenReturn("token");

        Request actual = extasyPayQrNspkMerchantOrderCreationService.body(detailsRequest);

        assertEquals("https://gateway.example.com/merchant-details/callback?merchant=EXTASY_PAY_QR_NSPK&secret=secret",
                actual.getNotificationUrl());
    }

    @Test
    void buildResponseShouldSetQrForSbpQrDeal() {
        Response response = new Response();
        DealDTO dealDTO = new DealDTO();
        dealDTO.setPaymentOption(Method.SBP_QR);
        dealDTO.setQrCodeLink("https://www.sberbank.com/sms/pbpn?requisiteNumber=4444444444444444");
        response.setDeals(List.of(dealDTO));
        response.setId("9e6eee50-dcbb-47be-85a5-494db6d49157");

        Optional<DetailsResponse> actual = extasyPayQrNspkMerchantOrderCreationService.buildResponse(response);

        assertTrue(actual.isPresent());
        DetailsResponse actualResponse = actual.get();
        assertAll(
                () -> assertEquals(Merchant.EXTASY_PAY_QR_NSPK, actualResponse.getMerchant()),
                () -> assertEquals(
                        "https://www.sberbank.com/sms/pbpn?requisiteNumber=4444444444444444", actualResponse.getQr()),
                () -> assertEquals("SBP_QR", actualResponse.getPaymentMethod())
        );
    }
}
