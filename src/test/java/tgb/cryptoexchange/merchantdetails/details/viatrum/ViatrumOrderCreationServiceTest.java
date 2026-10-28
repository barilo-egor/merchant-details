package tgb.cryptoexchange.merchantdetails.details.viatrum;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.exception.BodyMappingException;
import tgb.cryptoexchange.merchantdetails.properties.ViatrumProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViatrumOrderCreationServiceTest {

    @Mock
    private ViatrumProperties viatrumProperties;

    @InjectMocks
    private ViatrumOrderCreationService viatrumOrderCreationService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SignatureService signatureService;

    @Test
    void getMerchantShouldReturnViatrum() {
        assertEquals(Merchant.VIATRUM, viatrumOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddUriPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/v1/pay-in", viatrumOrderCreationService.uriBuilder(null).apply(uriBuilder).getPath());
    }


    @Test
    void headers_ShouldAddCorrectHeaders() throws Exception {
        String body = "{\"some\":\"json\"}";
        String nonce = "12345";
        String signature = "expected-signature";

        Request requestMock = new Request();
        requestMock.setExternalID(nonce);

        when(viatrumProperties.pub()).thenReturn("test-pub");
        when(viatrumProperties.environment()).thenReturn("test-env");
        when(viatrumProperties.secret()).thenReturn("test-secret");

        when(objectMapper.readValue(body, Request.class)).thenReturn(requestMock);
        when(signatureService.generateHmacSha512Signature(anyString(), eq("test-secret")))
                .thenReturn(signature);

        HttpHeaders httpHeaders = new HttpHeaders();

        Consumer<HttpHeaders> headersConsumer = viatrumOrderCreationService.headers(new OrderCreationRequest(), body);
        headersConsumer.accept(httpHeaders);

        assertEquals(nonce, httpHeaders.getFirst("nonce"));
        assertEquals("application/json", httpHeaders.getFirst("Content-Type"));
        assertEquals("test-pub", httpHeaders.getFirst("Public-Key"));
        assertEquals("test-env", httpHeaders.getFirst("X-Environment"));
        assertEquals(signature, httpHeaders.getFirst("Signature"));

        String expectedCanonical = viatrumOrderCreationService.buildCanonicalString(body, nonce);
        verify(signatureService).generateHmacSha512Signature(eq(expectedCanonical), eq("test-secret"));
    }

    @Test
    void headers_ShouldThrowException_WhenJsonInvalid() throws Exception {
        String body = "invalid-json";
        when(objectMapper.readValue(anyString(), eq(Request.class)))
                .thenThrow(new JsonProcessingException("error") {
                });

        Consumer<HttpHeaders> consumer = viatrumOrderCreationService.headers(new OrderCreationRequest(), body);
        HttpHeaders headers = new HttpHeaders();
        assertThrows(BodyMappingException.class, () -> consumer.accept(headers));
    }


    @Test
    void buildCanonicalString_ShouldFormatCorrectly() {
        String body = "{\"some\":\"json\"}";
        String nonce = "12345";
        String expectedHash = DigestUtils.sha256Hex(body);
        String expectedCanonical = "POST\n/api/v1/pay-in\n\n" + expectedHash + "\n" + nonce;

        assertEquals(expectedCanonical, viatrumOrderCreationService.buildCanonicalString(body, nonce));
    }

    @Test
    void buildCanonicalString_WithNullOrEmptyBody_ShouldUseEmptyStringHash() {
        String nonce = "12345";
        String emptyHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        assertEquals("POST\n/api/v1/pay-in\n\n" + emptyHash + "\n" + nonce,
                viatrumOrderCreationService.buildCanonicalString("", nonce));
        assertEquals("POST\n/api/v1/pay-in\n\n" + emptyHash + "\n" + nonce,
                viatrumOrderCreationService.buildCanonicalString(null, nonce));
    }

    @Test
    void nextNonceShouldBeMillisWithinFiveMinutesInFuture() {
        long before = System.currentTimeMillis();
        long nonce = viatrumOrderCreationService.nextNonce();
        long after = System.currentTimeMillis();

        assertTrue(nonce > after);
        assertTrue(nonce <= before + 5 * 60_000);
    }

    @Test
    void nextNonceShouldStrictlyIncrease() {
        long previous = viatrumOrderCreationService.nextNonce();
        for (int i = 0; i < 1000; i++) {
            long next = viatrumOrderCreationService.nextNonce();
            assertTrue(next > previous);
            previous = next;
        }
    }
}
