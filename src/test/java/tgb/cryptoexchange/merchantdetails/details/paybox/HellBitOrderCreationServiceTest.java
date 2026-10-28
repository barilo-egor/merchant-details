package tgb.cryptoexchange.merchantdetails.details.paybox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.HellBitProperties;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HellBitOrderCreationServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private HellBitProperties hellBitProperties;

    @InjectMocks
    private HellBitOrderCreationService service;

    @Test
    void getMerchantShouldReturnHellBit() {
        assertEquals(Merchant.HELLBIT, service.getMerchant());
    }

    @Test
    void buildResponseShouldSetBankAndDetailsForPhoneNumber() {
        Response response = new Response();
        response.setId(101L);
        response.setBankName("Sberbank");
        response.setPhoneNumber("+79991112233");

        Optional<DetailsResponse> result = service.buildResponse(response);
        assertTrue(result.isPresent());
        DetailsResponse detailsResponse = result.get();

        assertAll(
                () -> assertEquals("Sberbank", detailsResponse.getBank()),
                () -> assertEquals("+79991112233", detailsResponse.getDetails()),
                () -> assertEquals("101", detailsResponse.getMerchantOrderId()),
                () -> assertEquals(Status.PROCESS.name(), detailsResponse.getMerchantOrderStatus()),
                () -> assertEquals(Merchant.HELLBIT, detailsResponse.getMerchant()),
                () -> assertNull(detailsResponse.getQr())
        );
    }

    @Test
    void buildResponseShouldSetBankAndDetailsForCardNumber() {
        Response response = new Response();
        response.setId(102L);
        response.setBankName("T-Bank");
        response.setCardNumber("2200123412341234");

        Optional<DetailsResponse> result = service.buildResponse(response);
        assertTrue(result.isPresent());
        DetailsResponse detailsResponse = result.get();

        assertAll(
                () -> assertEquals("T-Bank", detailsResponse.getBank()),
                () -> assertEquals("2200123412341234", detailsResponse.getDetails()),
                () -> assertEquals("102", detailsResponse.getMerchantOrderId()),
                () -> assertEquals(Status.PROCESS.name(), detailsResponse.getMerchantOrderStatus()),
                () -> assertEquals(Merchant.HELLBIT, detailsResponse.getMerchant()),
                () -> assertNull(detailsResponse.getQr())
        );
    }

    @Test
    void buildResponseShouldSetQrForPaymentLink() {
        Response response = new Response();
        response.setId(103L);
        response.setPaymentLink("https://pay.example.com/link");

        Optional<DetailsResponse> result = service.buildResponse(response);
        assertTrue(result.isPresent());
        DetailsResponse detailsResponse = result.get();

        assertAll(
                () -> assertNull(detailsResponse.getBank()),
                () -> assertNull(detailsResponse.getDetails()),
                () -> assertEquals("https://pay.example.com/link", detailsResponse.getQr())
        );
    }
}
