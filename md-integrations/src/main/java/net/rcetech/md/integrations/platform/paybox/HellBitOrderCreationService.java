package net.rcetech.md.integrations.platform.paybox;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.properties.HellBitProperties;

import java.util.Objects;
import java.util.Optional;

@Service
public class HellBitOrderCreationService extends PayBoxOrderCreationService {

    private final HellBitProperties hellBitProperties;

    protected HellBitOrderCreationService(@Qualifier("hellBitRestClient") RestClient restClient,
                                          HellBitProperties hellBitProperties) {
        super(restClient, hellBitProperties);
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

        if (Objects.nonNull(response.getPhoneNumber()) || Objects.nonNull(response.getCardNumber())) {
            detailsResponse.setBank(response.getBankName());
            if (Objects.nonNull(response.getPhoneNumber())) {
                detailsResponse.setDetails(response.getPhoneNumber());
            } else {
                detailsResponse.setDetails(response.getCardNumber());
            }
        } else if (Objects.nonNull(response.getPaymentLink())) {
            detailsResponse.setQr(response.getPaymentLink());
        }

        detailsResponse.setMerchantOrderStatus(Status.PROCESS.name());
        return Optional.of(detailsResponse);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.HELLBIT;
    }

}
