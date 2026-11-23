package net.rcetech.md.integrations.platform.bridgepay;

import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.ExtasyPayPdfProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtasyPayPdfMerchantOrderCreationServiceTest {

    @Mock
    private ExtasyPayPdfProperties extasyPayPdfProperties;

    @Mock
    private CallbackConfig callbackConfig;

    @InjectMocks
    private ExtasyPayPdfMerchantOrderCreationService extasyPayPdfMerchantOrderCreationService;

    @Test
    void getMerchantShouldReturnExtasyPayPdf() {
        assertEquals(Merchant.EXTASY_PAY_PDF, extasyPayPdfMerchantOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldSetPath() {
        var uriBuilder = org.springframework.web.util.UriComponentsBuilder.newInstance();
        assertEquals("/api/merchant/invoices",
                extasyPayPdfMerchantOrderCreationService.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @Test
    void bodyShouldBuildRequestObject() {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(1000);
        detailsRequest.setMethod(Method.SBP.name());
        when(callbackConfig.getGatewayUrl()).thenReturn("https://gateway.example.com");
        when(callbackConfig.getCallbackSecret()).thenReturn("secret");
        when(extasyPayPdfProperties.token()).thenReturn("token");

        Request actual = extasyPayPdfMerchantOrderCreationService.body(detailsRequest);

        assertAll(
                () -> assertEquals("1000", actual.getAmount()),
                () -> assertEquals("RUB", actual.getCurrency()),
                () -> assertEquals("https://gateway.example.com/merchant-details/callback?merchant=EXTASY_PAY_PDF&secret=secret",
                        actual.getNotificationUrl()),
                () -> assertEquals("token", actual.getNotificationToken()),
                () -> assertDoesNotThrow(() -> UUID.fromString(actual.getInternalId())),
                () -> assertEquals(Method.SBP, actual.getPaymentOption()),
                () -> assertTrue(actual.getStartDeal())
        );
    }

    @Test
    void buildResponseShouldReturnMappedRequisiteDTO() {
        Response response = new Response();
        DealDTO dealDTO = new DealDTO();
        dealDTO.setPaymentMethod(Bank.SBERBANK.getValue());
        dealDTO.setPaymentOption(Method.TO_CARD);
        RequisitesDTO requisitesDTO = new RequisitesDTO();
        requisitesDTO.setRequisites("4444 4444 4444 4444");
        dealDTO.setRequisites(requisitesDTO);
        response.setDeals(List.of(dealDTO));
        response.setId("2ac093d0-49aa-4bc7-a7dc-61fa62820544");

        Optional<DetailsResponse> actual = extasyPayPdfMerchantOrderCreationService.buildResponse(response);

        assertTrue(actual.isPresent());
        DetailsResponse actualResponse = actual.get();
        assertAll(
                () -> assertEquals(Merchant.EXTASY_PAY_PDF, actualResponse.getMerchant()),
                () -> assertEquals("2ac093d0-49aa-4bc7-a7dc-61fa62820544", actualResponse.getMerchantOrderId()),
                () -> assertEquals(Status.NEW.name(), actualResponse.getMerchantOrderStatus()),
                () -> assertEquals("4444 4444 4444 4444", actualResponse.getDetails()),
                () -> assertEquals(Bank.SBERBANK.getDisplayName(), actualResponse.getBank()),
                () -> assertEquals("TO_CARD", actualResponse.getPaymentMethod())
        );
    }

    @Test
    void getCallbackTypeShouldReturnBridgeCallback() {
        assertEquals(BridgeCallback.class, extasyPayPdfMerchantOrderCreationService.getCallbackType());
    }
}
