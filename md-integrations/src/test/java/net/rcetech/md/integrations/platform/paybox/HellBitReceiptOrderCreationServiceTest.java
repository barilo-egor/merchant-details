package net.rcetech.md.integrations.platform.paybox;

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
import net.rcetech.md.integrations.util.MultipartFormBuilder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestClient;
import org.springframework.util.MultiValueMap;
import org.springframework.http.HttpEntity;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.properties.HellBitReceiptProperties;
import net.rcetech.md.integrations.service.RequestService;

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
    private RestClient restClient;

    @Captor
    private ArgumentCaptor<Consumer<HttpHeaders>> headersCaptor;

    @Captor
    private ArgumentCaptor<Function<UriBuilder, URI>> uriBuilderCaptor;

    @Captor
    private ArgumentCaptor<MultiValueMap<String, HttpEntity<?>>> bodyInserterCaptor;

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
        verify(requestService).requestAsync(
                eq(restClient),
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
        MultipartFormBuilder bodyBuilder = new MultipartFormBuilder();

        hellBitReceiptOrderCreationService.addOrderIdPart(bodyBuilder, "123456");

        assertTrue(bodyBuilder.build().isEmpty());
    }

    @Test
    void getMerchantShouldReturnHellBitReceipt() {
        assertEquals(Merchant.HELLBIT_RECEIPT, hellBitReceiptOrderCreationService.getMerchant());
    }
}
