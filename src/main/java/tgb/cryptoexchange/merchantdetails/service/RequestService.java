package tgb.cryptoexchange.merchantdetails.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class RequestService {

    public String request(WebClient webClient, HttpMethod httpMethod, Function<UriBuilder, URI> uriBuilder,
                          Consumer<HttpHeaders> headersConsumer, String body) {
        return webClient.method(httpMethod)
                .uri(uriBuilder)
                .headers(headersConsumer)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
