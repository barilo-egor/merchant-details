package tgb.cryptoexchange.merchantdetails.details.gambit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.HesoyamSimProperties;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HesoyamSimOrderCreationServiceTest {

    @Mock
    private WebClient hesoyamWebClient;

    @Mock
    private HesoyamSimProperties hesoyamSimProperties;

    @InjectMocks
    private HesoyamSimOrderCreationService service;

    @Test
    void getMerchantShouldReturnHesoyamSim() {
        assertEquals(Merchant.HESOYAM_SIM, service.getMerchant());
    }

    @Test
    void buildResponseShouldSetBankAndDetailsCorrectly() {
        Response response = new Response();
        response.setId("hesoyam-sim-100");
        response.setStatus(Status.SUCCEEDED);
        response.setAmount(1500.0);

        Response.Requisites requisite = new Response.Requisites();
        requisite.setOperator("BEELINE");
        requisite.setPhone("+79998887766");
        response.setPaymentDetails(requisite);

        Optional<DetailsResponse> maybeResponse = service.buildResponse(response);
        assertTrue(maybeResponse.isPresent());

        DetailsResponse actual = maybeResponse.get();
        assertAll(
                () -> assertEquals(1500, actual.getAmount()),
                () -> assertEquals("hesoyam-sim-100", actual.getMerchantOrderId()),
                () -> assertEquals(Status.SUCCEEDED.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals("BEELINE", actual.getBank()),
                () -> assertEquals("+79998887766", actual.getDetails()),
                () -> assertEquals(Merchant.HESOYAM_SIM, actual.getMerchant())
        );
    }
}
