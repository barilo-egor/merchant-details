package tgb.cryptoexchange.merchantdetails.mapper;

import org.springframework.stereotype.Component;
import tgb.cryptoexchange.grpc.generated.DetailsGrpc;
import tgb.cryptoexchange.grpc.generated.DetailsRequestGrpc;
import tgb.cryptoexchange.grpc.generated.DetailsResponseGrpc;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.ApiDetailsRequest;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.ApiDetailsResponse;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;

import java.util.List;

@Component
public class ApiDetailsRequestMapper {

    public ApiDetailsRequest mapGrpcToDto(DetailsRequestGrpc grpc) {
        ApiDetailsRequest apiDetailsRequest = new ApiDetailsRequest();
        if (grpc.hasRequestId()) {
            apiDetailsRequest.setRequestId(grpc.getRequestId().getValue());
        } else {
            throwInvalidArgumentException("requestId");
        }
        if (grpc.hasInternalId()) {
            apiDetailsRequest.setInternalId(grpc.getInternalId().getValue());
        } else {
            throwInvalidArgumentException("internalId");
        }
        if (grpc.hasUserId()) {
            apiDetailsRequest.setUserId(grpc.getUserId().getValue());
        } else {
            throwInvalidArgumentException("userId");
        }
        if (grpc.hasAmount()) {
            apiDetailsRequest.setAmount(grpc.getAmount().getValue());
        } else {
            throwInvalidArgumentException("amount");
        }
        if (grpc.getRequestMethodList().isEmpty()) {
            throwInvalidArgumentException("requestMethod");
        }
        List<RequestMethod> methods = grpc.getRequestMethodList().stream()
                .map(RequestMethod::valueOf)
                .toList();
        apiDetailsRequest.setRequestMethods(methods);
        return apiDetailsRequest;
    }

    private void throwInvalidArgumentException(String field) {
        throw GrpcValidator.invalidArgument(field, "Should not be empty.");
    }

    public DetailsResponseGrpc mapToGrpcDto(ApiDetailsResponse response) {
        return DetailsResponseGrpc.newBuilder()
                .setRequestId(response.getRequestId())
                .setMerchant(response.getMerchant())
                .setOrderId(response.getOrderId())
                .setOrderStatus(response.getOrderStatus())
                .setAmount(response.getAmount())
                .setDetails(DetailsGrpc.newBuilder()
                        .setRequestMethod(response.getDetails().getRequestMethod().name())
                        .setDetails(response.getDetails().getDetails())
                        .setBank(response.getDetails().getBank())
                        .setOperator(response.getDetails().getOperator())
                        .build())
                .build();
    }
}
