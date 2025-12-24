package tgb.cryptoexchange.merchantdetails.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class RequestService {

    public String request(WebClient webClient, HttpMethod httpMethod, Function<UriBuilder, URI> uriBuilder,
                          Consumer<HttpHeaders> headersConsumer, String body) {
        WebClient.RequestBodySpec requestBodySpec = webClient.method(httpMethod)
                .uri(uriBuilder)
                .headers(headersConsumer);
        if (Objects.nonNull(body)) {
            requestBodySpec.bodyValue(body);
        }
        return requestBodySpec
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(30));
    }

    public void request(WebClient webClient, HttpMethod httpMethod, Function<UriBuilder, URI> uriBuilder,
                        Consumer<HttpHeaders> headersConsumer, BodyInserters.MultipartInserter body,
                        Consumer<? super Throwable> onError) {
        WebClient.RequestBodyUriSpec method = webClient.method(httpMethod);
        method.uri(uriBuilder)
                .headers(headersConsumer)
                .body(body)
                .retrieve()
                .toBodilessEntity()
                .doOnError(onError)
                .subscribe();
    }
}
