package tgb.cryptoexchange.merchantdetails.details.paybox;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.HellBitBTProperties;

import java.util.Objects;
import java.util.Optional;

@Service
public class HellBitBtOrderCreationService extends PayBoxOrderCreationService {

    private final HellBitBTProperties hellBitProperties;

    protected HellBitBtOrderCreationService(@Qualifier("hellBitWebClient") WebClient webClient,
                                            HellBitBTProperties hellBitProperties) {
        super(webClient, hellBitProperties);
        this.hellBitProperties = hellBitProperties;
    }

    @Override
    protected void addHeaders(HttpHeaders httpHeaders, Method method) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Authorization", "Bearer " + hellBitProperties.token());
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getId().toString());

        if (Objects.nonNull(response.getPhoneNumber())) {
            detailsResponse.setDetails(response.getBankName() + " " + response.getPhoneNumber());
        } else if (Objects.nonNull(response.getCardNumber())) {
            detailsResponse.setDetails(response.getBankName() + " " + response.getCardNumber());
        } else if (Objects.nonNull(response.getPaymentLink())) {
            detailsResponse.setQr(response.getPaymentLink());
        }

        detailsResponse.setMerchantOrderStatus(Status.PROCESS.name());
        return Optional.of(detailsResponse);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.HELLBIT_BT;
    }

}
