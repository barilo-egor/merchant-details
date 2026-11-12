package tgb.cryptoexchange.merchantdetails.mapper;

import org.junit.jupiter.api.Test;
import tgb.cryptoexchange.grpc.generated.DetailsResponseGrpc;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.ApiDetailsResponse;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.Details;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;

import static org.junit.jupiter.api.Assertions.*;

class ApiDetailsRequestMapperTest {

    private final ApiDetailsRequestMapper mapper = new ApiDetailsRequestMapper();

    private ApiDetailsResponse response(Integer amount, String bank) {
        ApiDetailsResponse response = new ApiDetailsResponse();
        response.setMerchant("ONLY_PAYS");
        response.setOrderId("order-1");
        response.setOrderStatus("PENDING");
        response.setAmount(amount);
        response.setDetails(Details.builder()
                .requestMethod(RequestMethod.CARD)
                .details("1234 1234 1234 1234")
                .bank(bank)
                .build());
        return response;
    }

    @Test
    void mapToGrpcDtoShouldSkipNullAmountAndBank() {
        DetailsResponseGrpc actual = mapper.mapToGrpcDto(response(null, null));
        assertAll(
                () -> assertFalse(actual.hasAmount()),
                () -> assertFalse(actual.getDetails().hasBank()),
                () -> assertEquals("ONLY_PAYS", actual.getMerchant()),
                () -> assertEquals("1234 1234 1234 1234", actual.getDetails().getDetails())
        );
    }

    @Test
    void mapToGrpcDtoShouldSetAmountAndBank() {
        DetailsResponseGrpc actual = mapper.mapToGrpcDto(response(2000, "Сбербанк"));
        assertAll(
                () -> assertTrue(actual.hasAmount()),
                () -> assertEquals(2000, actual.getAmount()),
                () -> assertEquals("Сбербанк", actual.getDetails().getBank()),
                () -> assertEquals(RequestMethod.CARD.name(), actual.getDetails().getRequestMethod())
        );
    }
}
