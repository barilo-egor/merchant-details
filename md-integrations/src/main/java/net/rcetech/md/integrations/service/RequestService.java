package net.rcetech.md.integrations.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class RequestService {

    private final TaskExecutor asyncRequestExecutor;

    public RequestService(@Qualifier("asyncRequestExecutor") TaskExecutor asyncRequestExecutor) {
        this.asyncRequestExecutor = asyncRequestExecutor;
    }

    public String request(RestClient restClient, HttpMethod httpMethod, Function<UriBuilder, URI> uriBuilder,
                          Consumer<HttpHeaders> headersConsumer, String body) {
        RestClient.RequestBodySpec requestBodySpec = restClient.method(httpMethod)
                .uri(uriBuilder)
                .headers(headersConsumer);
        if (Objects.nonNull(body)) {
            requestBodySpec.body(body);
        }
        return requestBodySpec
                .retrieve()
                .body(String.class);
    }

    /**
     * Асинхронно отправляет multipart-запрос, не дожидаясь ответа.
     *
     * @param onError обработчик ошибки запроса, вызывается в потоке отправки
     */
    public void requestAsync(RestClient restClient, HttpMethod httpMethod, Function<UriBuilder, URI> uriBuilder,
                             Consumer<HttpHeaders> headersConsumer, MultiValueMap<String, HttpEntity<?>> body,
                             Consumer<? super Throwable> onError) {
        execute(restClient, httpMethod, uriBuilder, headersConsumer, body, onError);
    }

    /**
     * Асинхронно отправляет запрос с телом в виде массива байт, не дожидаясь ответа.
     *
     * @param onError обработчик ошибки запроса, вызывается в потоке отправки
     */
    public void requestAsync(RestClient restClient, HttpMethod httpMethod, Function<UriBuilder, URI> uriBuilder,
                             Consumer<HttpHeaders> headersConsumer, byte[] body,
                             Consumer<? super Throwable> onError) {
        execute(restClient, httpMethod, uriBuilder, headersConsumer, body, onError);
    }

    private void execute(RestClient restClient, HttpMethod httpMethod, Function<UriBuilder, URI> uriBuilder,
                         Consumer<HttpHeaders> headersConsumer, Object body, Consumer<? super Throwable> onError) {
        try {
            asyncRequestExecutor.execute(() -> {
                try {
                    restClient.method(httpMethod)
                            .uri(uriBuilder)
                            .headers(headersConsumer)
                            .body(body)
                            .retrieve()
                            .toBodilessEntity();
                } catch (Exception e) {
                    onError.accept(e);
                }
            });
        } catch (TaskRejectedException e) {
            onError.accept(e);
        }
    }
}
