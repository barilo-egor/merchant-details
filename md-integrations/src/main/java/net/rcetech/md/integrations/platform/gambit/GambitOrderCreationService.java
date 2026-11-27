package net.rcetech.md.integrations.platform.gambit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.client.RestClient;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.MerchantOrderCreationService;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.platform.PlatformCurrencies;
import net.rcetech.md.integrations.properties.GambitProperties;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class GambitOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    protected final GambitProperties gambitProperties;

    private final Merchant merchant;

    public GambitOrderCreationService(Merchant merchant, RestClient restClient, GambitProperties gambitProperties) {
        super(restClient, Response.class, Callback.class);
        this.merchant = merchant;
        this.gambitProperties = gambitProperties;
    }

    @Override
    public Merchant getMerchant() {
        return merchant;
    }

    @Override
    public Function<UriBuilder, URI> uriBuilder(OrderCreationRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/orders/init").build();
    }

    @Override
    public Consumer<HttpHeaders> headers(OrderCreationRequest detailsRequest, String body) {
        return this::addHeaders;
    }

    private void addHeaders(HttpHeaders headers) {
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + gambitProperties.key());
    }

    @Override
    protected Request body(OrderCreationRequest detailsRequest) {
        Request request = new Request();
        request.setCurrency(PlatformCurrencies.code(getMerchant()));
        request.setMethod(parseDetailsMethod(detailsRequest.getMethod()));
        request.setOrderId(UUID.randomUUID().toString() + System.currentTimeMillis());
        request.setAmount(detailsRequest.getAmount());
        request.setTerminalUid(gambitProperties.terminal());
        return request;
    }

    /**
     * У аккаунтов площадки gambit разные наборы методов платформы: у hesoyam свои uid'ы
     * (HesoyamMethod), поэтому тип метода выбирается по мерчанту сервиса.
     */
    protected Method parseDetailsMethod(String method) {
        if (Merchant.HESOYAM == getMerchant()) {
            return parseMethod(method, HesoyamMethod.class);
        }
        return parseMethod(method, GambitMethod.class);
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setAmount(response.getAmount().intValue());
        Response.Requisites requisites = response.getPaymentDetails();
        detailsResponse.setBank(requisites.getBankName());
        if (Objects.nonNull(response.getPaymentDetails().getPhone())) {
            detailsResponse.setDetails(requisites.getPhone());
        } else {
            detailsResponse.setDetails(requisites.getCardNumber());
        }
        return Optional.of(detailsResponse);
    }
}
