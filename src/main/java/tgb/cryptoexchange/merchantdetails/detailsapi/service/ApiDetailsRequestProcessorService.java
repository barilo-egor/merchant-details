package tgb.cryptoexchange.merchantdetails.detailsapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.ApiDetailsRequest;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.ApiDetailsResponse;
import tgb.cryptoexchange.merchantdetails.mapper.GrpcValidator;

import java.util.Optional;

@Service
@Slf4j
public class ApiDetailsRequestProcessorService {

    private final ApiMerchantDetailsService merchantDetailsService;

    public ApiDetailsRequestProcessorService(ApiMerchantDetailsService merchantDetailsService) {
        this.merchantDetailsService = merchantDetailsService;
    }

    public ApiDetailsResponse process(ApiDetailsRequest detailsRequest) {
        Optional<ApiDetailsResponse> response = merchantDetailsService.getDetails(detailsRequest);
        if (response.isPresent()) {
            return response.get();
        }
        throw GrpcValidator.detailsNotFound(detailsRequest);
    }
}
