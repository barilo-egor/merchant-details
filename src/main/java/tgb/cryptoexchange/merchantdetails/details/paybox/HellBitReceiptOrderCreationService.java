package tgb.cryptoexchange.merchantdetails.details.paybox;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.properties.HellBitReceiptProperties;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class HellBitReceiptOrderCreationService extends PayBoxOrderCreationService {

    protected HellBitReceiptOrderCreationService(@Qualifier("hellBitWebClient") WebClient webClient,
                                                 HellBitReceiptProperties hellBitProperties) {
        super(webClient, hellBitProperties);
    }

    @Override
    protected Request body(OrderCreationRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        request.setMerchantTransactionId(UUID.randomUUID().toString());

        Method method = parseMethod(detailsRequest.getMethod(), Method.class);
        if (Method.ACCOUNT_PSB.equals(method)) {
            request.setPayerBankName(method.getBankName());
        }
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getId().toString());
        if (Objects.nonNull(response.getPaymentLink())) {
            detailsResponse.setQr(response.getPaymentLink());
        }
        detailsResponse.setMerchantOrderStatus(Status.PROCESS.name());
        return Optional.of(detailsResponse);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.HELLBIT_RECEIPT;
    }

}
