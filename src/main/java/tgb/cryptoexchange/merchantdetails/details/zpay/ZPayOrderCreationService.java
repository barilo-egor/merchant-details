package tgb.cryptoexchange.merchantdetails.details.zpay;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.properties.ZPayProperties;

import java.io.File;
import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class ZPayOrderCreationService extends MerchantOrderCreationService<Response, Callback> {

    private final ZPayProperties zPayProperties;

    protected ZPayOrderCreationService(@Qualifier("zPayWebClient") WebClient webClient,
                                       ZPayProperties zPayProperties) {
        super(webClient, Response.class, Callback.class);
        this.zPayProperties = zPayProperties;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.Z_PAY;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/merchant/payin").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("Authorization", "Bearer " + zPayProperties.token());
        };
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        request.setMethodType(parseMethod(detailsRequest.getCurrentMerchantMethod(), Method.class));
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse requisiteVO = new DetailsResponse();
        requisiteVO.setMerchant(getMerchant());
        requisiteVO.setMerchantOrderStatus(Status.INITIATED.name());
        requisiteVO.setMerchantOrderId(String.valueOf(response.getId()));
        requisiteVO.setDetails(response.getBankName() + " " + response.getNumber());
        return Optional.of(requisiteVO);
    }

    @Override
    public void sendReceipt(String orderId, byte[] fileContent, String fileName) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("deal_id",orderId)
                .addFormDataPart("file","Tovarnyy-chek-blank.pdf",
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                new File("/root/tgb/Tovarnyy-chek-blank.pdf")))
                .build();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://api.zpay-1.xyz/api/merchant/disputes")
                .method("POST", body)
                .addHeader("Authorization", "Bearer 9012|r9Ga8i8sbDpd7aGuEWxaBEpAvXQKPHHqCd0nEuwA")
                .build();
        try (okhttp3.Response response = client.newCall(request).execute()){

        } catch (Exception e) {
            log.error("Ошибка отправки чека z-pay: {}", e.getMessage(), e);
        }
    }
}
