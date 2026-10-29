package tgb.cryptoexchange.merchantdetails.details.paybox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.HellBitBTProperties;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HellBitBtOrderCreationServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private HellBitBTProperties hellBitProperties;

    @InjectMocks
    private HellBitBtOrderCreationService service;

    @Test
    void getMerchantShouldReturnHellBitBt() {
        assertEquals(Merchant.HELLBIT_BT, service.getMerchant());
    }

    @Test
    void buildResponseShouldSetBankAndDetailsForPhoneNumber() {
        Response response = new Response();
        response.setId(201L);
        response.setBankName("Alfa-Bank");
        response.setPhoneNumber("+79992223344");

        Optional<DetailsResponse> result = service.buildResponse(response);
        assertTrue(result.isPresent());
        DetailsResponse detailsResponse = result.get();

        assertAll(
                () -> assertEquals("Alfa-Bank", detailsResponse.getBank()),
                () -> assertEquals("+79992223344", detailsResponse.getDetails()),
                () -> assertEquals("201", detailsResponse.getMerchantOrderId()),
                () -> assertEquals(Status.PROCESS.name(), detailsResponse.getMerchantOrderStatus()),
                () -> assertEquals(Merchant.HELLBIT_BT, detailsResponse.getMerchant()),
                () -> assertNull(detailsResponse.getQr())
        );
    }

    @Test
    void buildResponseShouldSetBankAndDetailsForCardNumber() {
        Response response = new Response();
        response.setId(202L);
        response.setBankName("VTB");
        response.setCardNumber("2200987698769876");

        Optional<DetailsResponse> result = service.buildResponse(response);
        assertTrue(result.isPresent());
        DetailsResponse detailsResponse = result.get();

        assertAll(
                () -> assertEquals("VTB", detailsResponse.getBank()),
                () -> assertEquals("2200987698769876", detailsResponse.getDetails()),
                () -> assertEquals("202", detailsResponse.getMerchantOrderId()),
                () -> assertEquals(Status.PROCESS.name(), detailsResponse.getMerchantOrderStatus()),
                () -> assertEquals(Merchant.HELLBIT_BT, detailsResponse.getMerchant()),
                () -> assertNull(detailsResponse.getQr())
        );
    }

    @Test
    void buildResponseShouldSetQrForPaymentLink() {
        Response response = new Response();
        response.setId(203L);
        response.setPaymentLink("https://pay.example.com/bt-link");

        Optional<DetailsResponse> result = service.buildResponse(response);
        assertTrue(result.isPresent());
        DetailsResponse detailsResponse = result.get();

        assertAll(
                () -> assertNull(detailsResponse.getBank()),
                () -> assertNull(detailsResponse.getDetails()),
                () -> assertEquals("https://pay.example.com/bt-link", detailsResponse.getQr())
        );
    }
}
