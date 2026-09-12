package tgb.cryptoexchange.merchantdetails.details.payscrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PayscrowTransgranOrderCreationServiceTest {

    @InjectMocks
    private PayscrowTransgranOrderCreationService service;

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals(
                "/api/v1/form/create",
                service.uriBuilder(null).apply(uriBuilder).getPath()
        );
    }

    @CsvSource(textBlock = """
            5220
            2552
            """)
    @ParameterizedTest
    void bodyShouldBuildRequestObject(Integer amount) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setCurrentMerchantMethod(Method.TRANS_SBP.name());
        Request request = service.body(detailsRequest);
        assertAll(
                () -> assertEquals(amount, request.getOrderData().getAmount()),
                () -> assertDoesNotThrow(() -> UUID.fromString(request.getOrderData().getClientOrderId())),
                () -> assertNull(request.getAmount()),
                () -> assertNull(request.getUniqueAmount())
        );
    }

    @Test
    void getMerchantShouldReturnPayscrowLow() {
        assertEquals(Merchant.PAYSCROW_TRANSGRAN, service.getMerchant());
    }
}