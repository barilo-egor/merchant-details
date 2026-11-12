package net.rcetech.md.integrations.platform.paylee;

import lombok.extern.slf4j.Slf4j;
import org.hashids.Hashids;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import net.rcetech.md.integrations.util.MultipartFormBuilder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.MerchantOrderCreationService;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.PayLeeProperties;
import net.rcetech.md.integrations.util.ClientIdUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
public abstract class PayLeeMerchantService extends MerchantOrderCreationService<Response, Callback> {

    static final String NON_FIELD_ERRORS = "nonFieldErrors";

    private final PayLeeProperties payLeeProperties;

    private final Hashids hashids;

    protected PayLeeMerchantService(RestClient restClient, PayLeeProperties payLeeProperties) {
        super(restClient, Response.class, Callback.class);
        this.payLeeProperties = payLeeProperties;
        this.hashids = new Hashids(payLeeProperties.clientIdSalt(), 8);
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/partners/purchases/").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(OrderCreationRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Authorization", "Token " + payLeeProperties.token());
            httpHeaders.add("Content-Type", "application/json");
        };
    }

    @Override
    protected Request body(OrderCreationRequest detailsRequest) {
        Request requestBody = new Request();
        requestBody.setPrice(detailsRequest.getAmount());
        requestBody.setRequisitesType(parseMethod(detailsRequest.getMethod(), Method.class));
        if (Objects.nonNull(detailsRequest.getUserId())) {
            requestBody.setClientId(ClientIdUtils.encode(hashids, detailsRequest.getUserId()));
        }
        return requestBody;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse vo = new DetailsResponse();
        if (Objects.nonNull(response.getRequisitesType()) && isNeedQR(response.getRequisitesType())) {
            vo.setQr(response.getRequisites());
        } else {
            vo.setBank(response.getBankName());
            vo.setDetails(response.getRequisites());
        }
        vo.setMerchant(getMerchant());
        vo.setMerchantOrderId(response.getId().toString());
        vo.setMerchantOrderStatus(response.getStatus().name());
        vo.setAmount(response.getPrice().intValue());
        return Optional.of(vo);
    }

    private boolean isNeedQR(List<Method> methods) {
        List<Method> merchants = Arrays.asList(Method.ANY_QR, Method.SBER_QR, Method.OZON_QR, Method.ALFA_QR, Method.GAZPROM_QR, Method.NSPK);
        return methods.stream().anyMatch(merchants::contains);
    }

    @Override
    protected Predicate<Exception> isNoDetailsExceptionPredicate() {
        return e -> {
            if (e instanceof HttpClientErrorException.BadRequest ex) {
                try {
                    JsonNode response = objectMapper.readTree(ex.getResponseBodyAsString());
                    return isNoTrader(response) || isAmountError(response) || isNoDetails(response);
                } catch (JacksonException jsonProcessingException) {
                    return false;
                }
            }
            return false;
        };
    }

    private boolean isAmountError(JsonNode response) {
        if (response.has("price")) {
            JsonNode price = response.get("price");
            return price.isArray() && price.size() == 1 && price.get(0).asString().startsWith("Убедитесь, что это значение");
        }
        return false;
    }

    private boolean isNoTrader(JsonNode response) {
        return response.has(NON_FIELD_ERRORS)
                && response.get(NON_FIELD_ERRORS).isArray()
                && response.get(NON_FIELD_ERRORS).size() == 1
                && response.get(NON_FIELD_ERRORS).get(0).asString()
                .equals("Нет доступного трейдера для вашего запроса. Попробуйте повторить позже.");
    }

    private boolean isNoDetails(JsonNode response) {
        return response.has(NON_FIELD_ERRORS)
                && response.get(NON_FIELD_ERRORS).isArray()
                && response.get(NON_FIELD_ERRORS).size() == 1
                && response.get(NON_FIELD_ERRORS).get(0).asString()
                .equals("Нет доступных реквизитов. Попробуйте повторить позже.");
    }

    @Override
    public void sendReceipt(String orderId, MultipartFile multipartFile) {
        MultipartFormBuilder builder = new MultipartFormBuilder();
        builder.part("attachment", multipartFile.getResource());

        requestService.requestAsync(
                restClient,
                HttpMethod.POST,
                uriBuilder -> uriBuilder.pathSegment("partners", "purchases", "{id}", "receipt").build(orderId),
                headers -> {
                    headers.add("Authorization", "Token " + payLeeProperties.token());
                    headers.add("Content-Type", "multipart/form-data");
                },
                builder.build(),
                t -> log.error("Ошибка отправки чека мерчанту PayLee по ордеру {}: {}", orderId, t.getMessage(), t)
        );
    }

    @Override
    public void sendReceipt(String orderId, byte[] fileContent, String fileName) {
        MultipartFormBuilder builder = new MultipartFormBuilder();
        builder.part("attachment", new ByteArrayResource(fileContent))
                .filename(fileName);

        requestService.requestAsync(
                restClient,
                HttpMethod.POST,
                uriBuilder -> uriBuilder.pathSegment("partners", "purchases", "{id}", "receipt").build(orderId),
                headers -> {
                    headers.add("Authorization", "Token " + payLeeProperties.token());
                    headers.add("Content-Type", "multipart/form-data");
                },
                builder.build(),
                t -> log.error("Ошибка отправки чека мерчанту PayLee по ордеру {}: {}", orderId, t.getMessage(), t)
        );
    }

}
