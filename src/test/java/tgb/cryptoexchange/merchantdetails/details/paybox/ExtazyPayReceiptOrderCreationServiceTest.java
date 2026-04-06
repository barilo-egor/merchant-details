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
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.ExtasyPayReceiptProperties;
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
class ExtazyPayReceiptOrderCreationServiceTest {

    @InjectMocks
    private ExtasyPayReceiptOrderCreationService extasyPayReceiptOrderCreationService;

    @Mock
    private ExtasyPayReceiptProperties extasyPayReceiptProperties;

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
    void sendReceiptShouldCallRequestServiceMethod(String orderId) {
        extasyPayReceiptOrderCreationService.setRequestService(requestService);
        when(extasyPayReceiptProperties.token()).thenReturn("token");

        extasyPayReceiptOrderCreationService.sendReceipt(orderId, "pdf-content".getBytes(), "receipt.pdf");

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
                () -> assertTrue(uri.getPath().endsWith("/attach"), "Путь должен заканчиваться на /attach"),
                () -> assertEquals("Bearer token", headers.getFirst("Authorization")),
                () -> assertNotNull(bodyInserterCaptor.getValue(), "BodyInserter не должен быть null")
        );
    }

    @Test
    void getMerchantShouldReturnExtazyPayReceipt() {
        assertEquals(Merchant.EXTASY_PAY_RECEIPT, extasyPayReceiptOrderCreationService.getMerchant());
    }
}