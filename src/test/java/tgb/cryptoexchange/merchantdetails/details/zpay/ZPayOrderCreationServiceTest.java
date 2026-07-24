package tgb.cryptoexchange.merchantdetails.details.zpay;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.ZPayProperties;
import tgb.cryptoexchange.merchantdetails.service.RequestService;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    void testSendReceipt_ShouldInvokeRequestWithCorrectData() throws InterruptedException, IOException {
        try (MockWebServer mockWebServer = new MockWebServer()) {
            mockWebServer.start();
            when(zPayProperties.url()).thenReturn(mockWebServer.url("/api").toString());
            when(zPayProperties.token()).thenReturn("test-token-123");
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("{\"status\":\"success\"}"));

            String orderId = "262768";
            String fileName = "receipt.pdf";
            byte[] content = "fake-pdf-content".getBytes();

            service.sendReceipt(orderId, content, fileName);
            RecordedRequest recordedRequest = mockWebServer.takeRequest();

            assertEquals("POST", recordedRequest.getMethod());
            assertEquals("/api/merchant/disputes", recordedRequest.getPath());
            assertEquals("Bearer test-token-123", recordedRequest.getHeader("Authorization"));

            String body = recordedRequest.getBody().readUtf8();
            assertTrue(body.contains("deal_id"));
            assertTrue(body.contains(orderId));
            assertTrue(body.contains(fileName));
        }
    }

}