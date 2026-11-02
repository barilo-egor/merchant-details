package tgb.cryptoexchange.merchantdetails.mapper;

import org.springframework.stereotype.Component;
import tgb.cryptoexchange.grpc.generated.DetailsGrpc;
import tgb.cryptoexchange.grpc.generated.DetailsRequestGrpc;
import tgb.cryptoexchange.grpc.generated.DetailsResponseGrpc;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.ApiDetailsRequest;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.ApiDetailsResponse;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class ApiDetailsRequestMapper {

    public ApiDetailsRequest mapGrpcToDto(DetailsRequestGrpc grpc) {
        ApiDetailsRequest apiDetailsRequest = new ApiDetailsRequest();
        if (!grpc.getInternalId().isEmpty()) {
            apiDetailsRequest.setInternalId(grpc.getInternalId());
        } else {
            throwInvalidArgumentException("internalId");
        }
        if (grpc.hasUserId() && !grpc.getUserId().isEmpty()) {
            apiDetailsRequest.setUserId(grpc.getUserId());
        }

        // 4. amount (int32 -> дефолтное значение 0, проверяем, что передано больше 0)
        if (grpc.getAmount() > 0) {
            apiDetailsRequest.setAmount(grpc.getAmount());
        } else {
            throwInvalidArgumentException("amount");
        }

        // 5. request_method (repeated -> проверка на isEmpty)
        if (grpc.getRequestMethodList().isEmpty()) {
            throwInvalidArgumentException("requestMethod");
        } else {
            List<RequestMethod> methods = grpc.getRequestMethodList().stream()
                    .map(RequestMethod::valueOf)
                    .toList();
            apiDetailsRequest.setRequestMethods(methods);
        }

        // 6. wait_timeout (int32 -> проверка на дефолтное значение)
        // Исправлен баг: теперь устанавливается сетер таймаута, а не requestId
        if (grpc.getWaitTimeout() > 0) {
            apiDetailsRequest.setWaitTimeout(grpc.getWaitTimeout());
        }

        // 7. owner_id (обычная string -> проверка на isEmpty)
        if (!grpc.getOwnerId().isEmpty()) {
            apiDetailsRequest.setOwnerId(UUID.fromString(grpc.getOwnerId()));
        }

        if (!grpc.getInitiatorApp().isEmpty()) {
            apiDetailsRequest.setInitiatorApp(grpc.getInitiatorApp());
        }

        return apiDetailsRequest;
    }


    private void throwInvalidArgumentException(String field) {
        throw GrpcValidator.invalidArgument(field, "Should not be empty.");
    }

    public DetailsResponseGrpc mapToGrpcDto(ApiDetailsResponse response) {
        DetailsGrpc.Builder detailsBuilder = DetailsGrpc.newBuilder()
                .setRequestMethod(response.getDetails().getRequestMethod().name())
                .setDetails(response.getDetails().getDetails())
                .setBank(response.getDetails().getBank());
        if (Objects.nonNull(response.getDetails().getBank())) {
            detailsBuilder.setBank(response.getDetails().getBank());
        }
        DetailsResponseGrpc.Builder builder = DetailsResponseGrpc.newBuilder()
                .setMerchant(response.getMerchant())
                .setOrderId(response.getOrderId())
                .setOrderStatus(response.getOrderStatus())
                .setAmount(response.getAmount())
                .setDetails(detailsBuilder.build());
        return builder.build();
    }
}
