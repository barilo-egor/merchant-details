package tgb.cryptoexchange.merchantdetails.detailsapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.ApiDetailsRequest;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.ApiDetailsResponse;

import java.util.Optional;

@Service
@Slf4j
public class ApiDetailRequestProcessorService {

    private final ThreadPoolTaskExecutor detailsRequestSearchExecutorApi;

    private final ApiMerchantDetailsService merchantDetailsService;

    public ApiDetailRequestProcessorService(ThreadPoolTaskExecutor detailsRequestSearchExecutorApi, ApiMerchantDetailsService merchantDetailsService) {
        this.detailsRequestSearchExecutorApi = detailsRequestSearchExecutorApi;
        this.merchantDetailsService = merchantDetailsService;
    }

    public void process(ApiDetailsRequest detailsRequest) {

        DetailsResponse result;
        try {
            Optional<ApiDetailsResponse> detailsResponse = merchantDetailsService.getDetails(detailsRequest);

        } catch (Exception e) {
            result = new DetailsResponse();
            result.setRequestId(detailsRequest.getRequestId());

        }

    }
}
