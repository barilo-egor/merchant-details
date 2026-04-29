package tgb.cryptoexchange.merchantdetails.details.zpay;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.ZPayProperties;
import tgb.cryptoexchange.merchantdetails.service.RequestService;

import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZPayOrderCreationServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private WebClient webClient;

    @Mock
    private ZPayProperties zPayProperties;

    private ZPayOrderCreationService service;

    @Mock
    private RequestService requestService;

    @BeforeEach
    void setUp() {
        service = new ZPayOrderCreationService(webClient, zPayProperties);
        ReflectionTestUtils.setField(service, "requestService", requestService);
        ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
    }

    @Test
    void getMerchant_ShouldReturnZPay() {
        assertEquals(Merchant.Z_PAY, service.getMerchant());
    }

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        URI resultUri = service.uriBuilder(null).apply(uriBuilder);
        assertEquals("/merchant/payin", resultUri.getPath());
    }

    @ValueSource(strings = {
            "iXyHJsfsffWW2zMDavNqGI", "nDqwHgf5OggggQX0hr5baF"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String token) {
        when(zPayProperties.token()).thenReturn(token);


        HttpHeaders headers = new HttpHeaders();
        service.headers(null, null).accept(headers);

        assertAll(
                () -> assertEquals("application/json", headers.getFirst("Content-Type")),
                () -> assertEquals("Bearer " + token, headers.getFirst("Authorization"))
        );

        verify(requestService, never()).request(any(), any(), any(), any(), any());
    }


    @Test
    void testBuildResponse_Success() {
        Response apiResponse = new Response();
        apiResponse.setId(1777);
        apiResponse.setBankName("Sberbank");
        apiResponse.setNumber("1234567890");

        Optional<DetailsResponse> result = service.buildResponse(apiResponse);

        assertTrue(result.isPresent());
        DetailsResponse details = result.get();
        assertEquals("1777", details.getMerchantOrderId());
        assertEquals("Sberbank 1234567890", details.getDetails());
        assertEquals("INITIATED", details.getMerchantOrderStatus());
    }

    @Test
    void testSendReceipt_ShouldInvokeRequestWithCorrectData() {
        String orderId = "12345";
        byte[] content = "hello".getBytes();
        String fileName = "test.pdf";

        service.sendReceipt(orderId, content, fileName);

        ArgumentCaptor<HttpMethod> methodCaptor = ArgumentCaptor.forClass(HttpMethod.class);

        verify(requestService).request(
                eq(webClient),
                methodCaptor.capture(),
                any(),
                any(),
                any(),
                any()
        );
        assertEquals(HttpMethod.POST, methodCaptor.getValue());
    }

}