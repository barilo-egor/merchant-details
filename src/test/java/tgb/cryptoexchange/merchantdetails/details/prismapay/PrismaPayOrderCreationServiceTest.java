package tgb.cryptoexchange.merchantdetails.details.prismapay;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.PrismaPayProperties;
import tgb.cryptoexchange.merchantdetails.service.ReceiptService;
import tgb.cryptoexchange.merchantdetails.service.RequestService;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrismaPayOrderCreationServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private PrismaPayProperties prismaPayProperties;
    @InjectMocks
    private PrismaPayOrderCreationService service;
    @Mock
    private RequestService requestService;
    @Mock
    private ReceiptService receiptService;
    @Mock
    private WebClient webClient;

    private String originalUserDir;

    @BeforeEach
    void setUp() {
        originalUserDir = System.getProperty("user.dir");
        service = new PrismaPayOrderCreationService(webClient, prismaPayProperties, receiptService);
        service.setRequestService(requestService);
        service.setObjectMapper(objectMapper);
    }

    @AfterEach
    void tearDown() {
        System.setProperty("user.dir", originalUserDir);
    }

    @Test
    void shouldReturnCorrectUriFromBuilder() {
        UriBuilder uriBuilder = mock(UriBuilder.class);
        URI expectedUri = URI.create("/api/orders");
        when(uriBuilder.path("/api/orders")).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(expectedUri);

        Function<UriBuilder, URI> function = service.uriBuilder(mock(DetailsRequest.class));
        URI actualUri = function.apply(uriBuilder);

        assertThat(actualUri).isEqualTo(expectedUri);
    }

    @Test
    void shouldAddCorrectHeaders() {
        when(prismaPayProperties.token()).thenReturn("test-token");
        HttpHeaders httpHeaders = new HttpHeaders();

        Consumer<HttpHeaders> headersConsumer = service.headers(mock(DetailsRequest.class), "body");
        headersConsumer.accept(httpHeaders);

        assertThat(httpHeaders.getFirst("Content-Type")).isEqualTo("application/json");
        assertThat(httpHeaders.getFirst("Authorization")).isEqualTo("Bearer test-token");
    }

    @Test
    void shouldMapDetailsRequestToMerchantRequest() {
        DetailsRequest detailsRequest = mock(DetailsRequest.class);
        when(detailsRequest.getAmount()).thenReturn(100);
        when(detailsRequest.getCurrentMerchantMethod()).thenReturn("CARD");

        Request request = service.body(detailsRequest);

        assertThat(request.getAmount()).isEqualTo(100);
        assertThat(request.getPaymentMethod()).isNotNull();
    }

    @Test
    void shouldBuildDetailsResponseFromMerchantResponse() {
        Response response = new Response();
        Response.Data data = new Response.Data();
        data.setId("123");
        data.setPaymentBank("Sberbank");
        data.setPaymentDetails("4276xxx");
        data.setStatus(Status.PENDING);
        response.setData(data);

        Optional<DetailsResponse> result = service.buildResponse(response);

        assertThat(result).isPresent();
        DetailsResponse detailsResponse = result.get();
        assertThat(detailsResponse.getMerchantOrderId()).isEqualTo("123");
        assertThat(detailsResponse.getDetails()).isEqualTo("Sberbank 4276xxx");
        assertThat(detailsResponse.getMerchant()).isEqualTo(Merchant.PRISMA_PAY);
        assertThat(detailsResponse.getMerchantOrderStatus()).isEqualTo("PENDING");
    }

    @Test
    void shouldCallRequestServiceOnCancel() {
        CancelOrderRequest cancelRequest = mock(CancelOrderRequest.class);
        service.makeCancelRequest(cancelRequest);

        verify(requestService).request(eq(webClient), eq(HttpMethod.POST), any(), any(), isNull());
    }

    @Test
    void shouldSaveReceiptAndSendJsonLink() {
        String orderId = "777";
        byte[] fileContent = "PDF_BYTES".getBytes();
        String fileName = "receipt.pdf";
        String stubbedLink = "http://localhost:8080/receipts/prisma/receipt.pdf";

        when(receiptService.saveReceipt(fileContent, fileName, StringUtils.lowerCase(Merchant.PRISMA_PAY.name()))).thenReturn(stubbedLink);
        service.sendReceipt(orderId, fileContent, fileName);

        String expectedJson = "{\"link\":\"" + stubbedLink + "\"}";
        verify(requestService).request(
                eq(webClient),
                eq(HttpMethod.POST),
                any(),
                any(),
                eq(expectedJson)
        );
        verify(receiptService).saveReceipt(fileContent, fileName, StringUtils.lowerCase(Merchant.PRISMA_PAY.name()));
    }

    @Test
    void getMerchantShouldReturnPrismaPay() {
        assertEquals(Merchant.PRISMA_PAY, service.getMerchant());
    }

}