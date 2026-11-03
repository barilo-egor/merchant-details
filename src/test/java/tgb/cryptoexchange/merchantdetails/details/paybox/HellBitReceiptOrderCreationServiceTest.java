package tgb.cryptoexchange.merchantdetails.details.paybox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.HellBitReceiptProperties;
import tgb.cryptoexchange.merchantdetails.service.RequestService;

import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HellBitReceiptOrderCreationServiceTest {

    @InjectMocks
    private HellBitReceiptOrderCreationService hellBitReceiptOrderCreationService;

    @Mock
    private HellBitReceiptProperties hellBitReceiptProperties;

    @Mock
    private WebClient webClient;

    @Captor
    private ArgumentCaptor<Consumer<HttpHeaders>> headersCaptor;

    @Captor
    private ArgumentCaptor<Function<UriBuilder, URI>> uriBuilderCaptor;

    @Captor
    private ArgumentCaptor<BodyInserters.MultipartInserter> bodyInserterCaptor;

    @Mock
    private RequestService requestService;

    @ValueSource(strings = {"123456", "9876543"})
    @ParameterizedTest
    void sendReceiptWithBytesShouldCallAttachByTransactionId(String orderId) {
        hellBitReceiptOrderCreationService.setRequestService(requestService);
        when(hellBitReceiptProperties.token()).thenReturn("token");

        hellBitReceiptOrderCreationService.sendReceipt(orderId, "pdf-content".getBytes(), "receipt.pdf");

        assertRequest(orderId);
    }

    @ValueSource(strings = {"123456", "9876543"})
    @ParameterizedTest
    void sendReceiptWithMultipartFileShouldCallAttachByTransactionId(String orderId) {
        hellBitReceiptOrderCreationService.setRequestService(requestService);
        when(hellBitReceiptProperties.token()).thenReturn("token");
        MockMultipartFile file = new MockMultipartFile("receipt", "receipt.png", "image/png", "png-content".getBytes());

        hellBitReceiptOrderCreationService.sendReceipt(orderId, file);

        assertRequest(orderId);
    }

    private void assertRequest(String orderId) {
        verify(requestService).request(
                eq(webClient),
                eq(HttpMethod.POST),
                uriBuilderCaptor.capture(),
                headersCaptor.capture(),
                bodyInserterCaptor.capture(),
                any()
        );

        URI uri = uriBuilderCaptor.getValue().apply(UriComponentsBuilder.newInstance());
        HttpHeaders headers = new HttpHeaders();
        headersCaptor.getValue().accept(headers);

        assertAll(
                () -> assertEquals("/api/v1/transactions/" + orderId + "/attach", uri.getPath()),
                () -> assertEquals("Bearer token", headers.getFirst("Authorization")),
                () -> assertNotNull(bodyInserterCaptor.getValue())
        );
    }

    @Test
    void addOrderIdPartShouldNotAddTransactionIdPart() {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();

        hellBitReceiptOrderCreationService.addOrderIdPart(bodyBuilder, "123456");

        assertTrue(bodyBuilder.build().isEmpty());
    }

    @Test
    void getMerchantShouldReturnHellBitReceipt() {
        assertEquals(Merchant.HELLBIT_RECEIPT, hellBitReceiptOrderCreationService.getMerchant());
    }
}
