package tgb.cryptoexchange.merchantdetails.mapper;

import com.google.protobuf.Any;
import com.google.rpc.BadRequest;
import com.google.rpc.Code;
import com.google.rpc.ResourceInfo;
import com.google.rpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.ApiDetailsRequest;

public class GrpcValidator {

    private GrpcValidator() {
    }

    public static StatusRuntimeException invalidArgument(String fieldName, String description) {
        BadRequest.FieldViolation violation = BadRequest.FieldViolation.newBuilder()
                .setField(fieldName)
                .setDescription(description)
                .build();

        BadRequest badRequest = BadRequest.newBuilder()
                .addFieldViolations(violation)
                .build();

        Status status = Status.newBuilder()
                .setCode(Code.INVALID_ARGUMENT_VALUE)
                .setMessage("Bad request")
                .addDetails(com.google.protobuf.Any.pack(badRequest))
                .build();

        return StatusProto.toStatusRuntimeException(status);
    }

    public static StatusRuntimeException detailsNotFound(ApiDetailsRequest request) {
        ResourceInfo resourceInfo = ResourceInfo.newBuilder()
                .setResourceType("ApiDetailsRequest")
                .setResourceName(request.getRequestId())
                .setDescription("Реквизиты для api-сделки получены не были")
                .build();

        Status status = Status.newBuilder()
                .setCode(Code.NOT_FOUND_VALUE)
                .setMessage("Details not found")
                .addDetails(Any.pack(resourceInfo))
                .build();
        return StatusProto.toStatusRuntimeException(status);
    }


}