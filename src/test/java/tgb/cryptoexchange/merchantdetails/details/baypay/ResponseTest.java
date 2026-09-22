package tgb.cryptoexchange.merchantdetails.details.baypay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Response response;
    private Response.Data validData;

    @BeforeEach
    void setUp() {
        response = new Response();

        validData = new Response.Data();
        validData.setId("inv_984312");
        validData.setAmount("15000");
        validData.setStatus(Status.ACTIVE);

        Response.Data.PaymentDetail paymentDetail = new Response.Data.PaymentDetail();
        paymentDetail.setBank("Tinkoff");
        paymentDetail.setDetail("2200700011112222");
        validData.setPaymentDetail(paymentDetail);
    }

    @Test
    void shouldPassValidationWhenResponseIsValid() {
        response.setSuccess(true);
        response.setData(validData);

        ValidationResult result = response.validate();

        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
    }

    @ParameterizedTest(name = "success = {0}")
    @NullSource
    @ValueSource(booleans = {false})
    void shouldReturnErrorWhenStatusIsNotTrue(Boolean success) {
        response.setSuccess(success);
        response.setData(validData);

        ValidationResult actual = response.validate();

        assertEquals(String.format("field \"success\" expected 'true' but was %s", success), actual.errorsToString());
    }

    @Test
    void shouldThrowNpeWhenDataIsNullAndStatusIsTrue() {
        response.setSuccess(true);
        response.setData(null);

        assertThrows(NullPointerException.class, () -> response.validate());
    }

    @Test
    void shouldReturnErrorsWhenAllDataFieldsAreNull() {
        response.setSuccess(true);
        response.setData(new Response.Data());

        ValidationResult actual = response.validate();

        assertAll(
                () -> assertTrue(actual.errorsToString().contains("field \"data.id\" must not be null")),
                () -> assertTrue(actual.errorsToString().contains("field \"data.amount\" must not be null")),
                () -> assertTrue(actual.errorsToString().contains("field \"data.status\" must not be null"))
        );
    }

    @Test
    void shouldReturnErrorWhenOnlyIdIsNull() {
        validData.setId(null);
        response.setSuccess(true);
        response.setData(validData);

        ValidationResult actual = response.validate();

        assertAll(
                () -> assertEquals("field \"data.id\" must not be null", actual.errorsToString())
        );
    }

    @Test
    void shouldReturnErrorWhenOnlyAmountIsNull() {
        validData.setAmount(null);
        response.setSuccess(true);
        response.setData(validData);

        ValidationResult actual = response.validate();

        assertAll(
                () -> assertEquals("field \"data.amount\" must not be null", actual.errorsToString())
        );
    }

    @Test
    void shouldReturnErrorWhenOnlyStatusIsNull() {
        validData.setStatus(null);
        response.setSuccess(true);
        response.setData(validData);

        ValidationResult actual = response.validate();

        assertAll(
                () -> assertEquals("field \"data.status\" must not be null", actual.errorsToString())
        );
    }

    @Test
    void shouldReturnTrueWhenAllDetailFieldsArePresent() {
        response.setData(validData);

        assertTrue(response.hasDetails());
    }

    @Test
    void shouldReturnFalseWhenDataIsNull() {
        response.setData(null);

        assertFalse(response.hasDetails());
    }

    @Test
    void shouldReturnFalseWhenPaymentDetailIsNull() {
        validData.setPaymentDetail(null);
        response.setData(validData);

        assertFalse(response.hasDetails());
    }

    @Test
    void shouldReturnFalseWhenBankIsNull() {
        validData.getPaymentDetail().setBank(null);
        response.setData(validData);

        assertFalse(response.hasDetails());
    }

    @Test
    void shouldReturnFalseWhenDetailIsNull() {
        validData.getPaymentDetail().setDetail(null);
        response.setData(validData);

        assertFalse(response.hasDetails());
    }

    @Test
    void shouldDeserializeCompleteValidJson() throws JsonProcessingException {
        String json = """
                {
                  "success": true,
                  "data": {
                    "id": "order-uuid-777",
                    "amount": "25000",
                    "status": "Активна",
                    "payment_details": {
                      "detail": "2200700199998888",
                      "bank": "Сбербанк"
                    }
                  }
                }
                """;

        Response actual = objectMapper.readValue(json, Response.class);

        assertAll(
                () -> assertNotNull(actual),
                () -> assertTrue(actual.getSuccess()),
                () -> assertNotNull(actual.getData()),
                () -> assertEquals("order-uuid-777", actual.getData().getId()),
                () -> assertEquals("25000", actual.getData().getAmount()),
                () -> assertEquals(Status.ACTIVE, actual.getData().getStatus()),
                () -> assertNotNull(actual.getData().getPaymentDetail()),
                () -> assertEquals("Сбербанк", actual.getData().getPaymentDetail().getBank()),
                () -> assertEquals("2200700199998888", actual.getData().getPaymentDetail().getDetail()),
                () -> assertTrue(actual.hasDetails()),
                () -> assertEquals("", actual.validate().errorsToString())
        );
    }

    @Test
    @DisplayName("Десериализация неизвестного статуса приводит к null и ошибке валидации")
    void shouldDeserializeUnknownStatusAsNull() throws JsonProcessingException {
        String json = """
                {
                  "success": true,
                  "data": {
                    "id": "order-123",
                    "amount": "1000",
                    "status": "UNKNOWN_OR_NEW_STATUS"
                  }
                }
                """;

        Response actual = objectMapper.readValue(json, Response.class);

        assertAll(
                () -> assertNotNull(actual.getData()),
                () -> assertNull(actual.getData().getStatus()),
                () -> assertEquals("field \"data.status\" must not be null", actual.validate().errorsToString())
        );
    }
}