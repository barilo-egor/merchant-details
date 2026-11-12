package net.rcetech.md.integrations.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import net.rcetech.md.integrations.config.RestClientsConfig;
import net.rcetech.md.integrations.util.MultipartFormBuilder;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class RequestServiceTest {

    private MockRestServiceServer server;

    private RestClient restClient;

    private RequestService requestService;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClientsConfig.get30SecondsReadTimeoutRestClientBuilder()
                .baseUrl("http://merchant.test");
        server = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();
        requestService = new RequestService(new SyncTaskExecutor());
    }

    @Test
    void requestShouldSendBodyAndHeadersAndReturnResponseBody() {
        server.expect(requestTo("http://merchant.test/orders"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer token"))
                .andExpect(content().string("{\"amount\":100}"))
                .andRespond(withSuccess("{\"id\":\"1\"}", MediaType.APPLICATION_JSON));

        String response = requestService.request(restClient, HttpMethod.POST,
                uriBuilder -> uriBuilder.path("/orders").build(),
                headers -> {
                    headers.add("Authorization", "Bearer token");
                    headers.setContentType(MediaType.APPLICATION_JSON);
                },
                "{\"amount\":100}");

        assertEquals("{\"id\":\"1\"}", response);
        server.verify();
    }

    @Test
    void requestShouldNotSendBodyIfBodyIsNull() {
        server.expect(requestTo("http://merchant.test/orders/1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(content().string(""))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        String response = requestService.request(restClient, HttpMethod.GET,
                uriBuilder -> uriBuilder.path("/orders/{id}").build("1"), headers -> {
                }, null);

        assertEquals("{}", response);
        server.verify();
    }

    @Test
    void requestShouldReadTextResponseWithoutCharsetAsUtf8() {
        server.expect(requestTo("http://merchant.test/orders"))
                .andRespond(withSuccess("Реквизиты не найдены".getBytes(StandardCharsets.UTF_8), MediaType.TEXT_PLAIN));

        String response = requestService.request(restClient, HttpMethod.GET,
                uriBuilder -> uriBuilder.path("/orders").build(), headers -> {
                }, null);

        assertEquals("Реквизиты не найдены", response);
    }

    @Test
    void requestShouldThrowBadRequestWithResponseBodyIfMerchantRespondedWith400() {
        server.expect(requestTo("http://merchant.test/orders"))
                .andRespond(withBadRequest().body("{\"message\":\"NO_REQUISITE\"}").contentType(MediaType.APPLICATION_JSON));

        HttpClientErrorException.BadRequest ex = assertThrows(HttpClientErrorException.BadRequest.class,
                () -> requestService.request(restClient, HttpMethod.POST,
                        uriBuilder -> uriBuilder.path("/orders").build(), headers -> {
                        }, "{}"));

        assertEquals("{\"message\":\"NO_REQUISITE\"}", ex.getResponseBodyAsString());
    }

    @Test
    void requestShouldThrowInternalServerErrorIfMerchantRespondedWith500() {
        server.expect(requestTo("http://merchant.test/orders"))
                .andRespond(withServerError());

        assertThrows(HttpServerErrorException.InternalServerError.class,
                () -> requestService.request(restClient, HttpMethod.POST,
                        uriBuilder -> uriBuilder.path("/orders").build(), headers -> {
                        }, "{}"));
    }

    @Test
    void requestAsyncShouldSendMultipartBody() {
        server.expect(requestTo("http://merchant.test/receipt"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                .andExpect(content().string(containsString("name=\"transaction_id\"")))
                .andExpect(content().string(containsString("name=\"receipts\"; filename=\"receipt.pdf\"")))
                .andExpect(content().string(containsString("Content-Type: application/pdf")))
                .andRespond(withSuccess());
        MultipartFormBuilder builder = new MultipartFormBuilder();
        builder.part("transaction_id", "123", MediaType.TEXT_PLAIN);
        builder.part("receipts", new ByteArrayResource("pdf".getBytes()))
                .filename("receipt.pdf")
                .contentType(MediaType.APPLICATION_PDF);
        AtomicReference<Throwable> error = new AtomicReference<>();

        requestService.requestAsync(restClient, HttpMethod.POST,
                uriBuilder -> uriBuilder.path("/receipt").build(), headers -> {
                }, builder.build(), error::set);

        server.verify();
        assertNull(error.get());
    }

    @Test
    void requestAsyncShouldSendByteArrayBody() {
        server.expect(requestTo("http://merchant.test/receipt"))
                .andExpect(content().bytes("raw".getBytes()))
                .andRespond(withSuccess());
        AtomicReference<Throwable> error = new AtomicReference<>();

        requestService.requestAsync(restClient, HttpMethod.POST,
                uriBuilder -> uriBuilder.path("/receipt").build(),
                headers -> headers.setContentType(MediaType.APPLICATION_OCTET_STREAM),
                "raw".getBytes(), error::set);

        server.verify();
        assertNull(error.get());
    }

    @Test
    void requestAsyncShouldPassMerchantErrorToHandler() {
        server.expect(requestTo("http://merchant.test/receipt"))
                .andRespond(withServerError());
        AtomicReference<Throwable> error = new AtomicReference<>();

        requestService.requestAsync(restClient, HttpMethod.POST,
                uriBuilder -> uriBuilder.path("/receipt").build(), headers -> {
                }, new MultipartFormBuilder().build(), error::set);

        assertInstanceOf(HttpServerErrorException.InternalServerError.class, error.get());
    }

    @Test
    void requestAsyncShouldPassRejectionToHandlerIfExecutorIsFull() {
        TaskExecutor rejectingExecutor = task -> {
            throw new TaskRejectedException("full");
        };
        RequestService service = new RequestService(rejectingExecutor);
        AtomicReference<Throwable> error = new AtomicReference<>();

        assertDoesNotThrow(() -> service.requestAsync(restClient, HttpMethod.POST,
                uriBuilder -> uriBuilder.path("/receipt").build(), headers -> {
                }, new MultipartFormBuilder().build(), error::set));

        assertInstanceOf(TaskRejectedException.class, error.get());
    }
}
