package tgb.cryptoexchange.merchantdetails.details.studio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ResponseTest {

    @Test
    @DisplayName("Успешный результат при заполнении всех обязательных полей")
    void validateShouldReturnNoErrorsIfAllFieldsPresent() {
        Response response = new Response();
        response.setAmount(0);
        response.setInternalId("internalId");
        response.setClientOrderId("clientOrderId");
        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    @DisplayName("Ошибка, если отсутствует internalId")
    void validateShouldReturnErrorIfInternalIdIsNull() {
        Response response = new Response();
        response.setAmount(0);
        response.setClientOrderId("clientOrderId");
        assertEquals("field \"internalId\" must not be null", response.validate().errorsToString());
    }

    @Test
    @DisplayName("Ошибка, если отсутствует clientOrderId")
    void validateShouldReturnErrorIfClientOrderIdIsNull() {
        Response response = new Response();
        response.setAmount(0);
        response.setInternalId("internalId");
        assertEquals("field \"clientOrderId\" must not be null", response.validate().errorsToString());
    }

    @Test
    @DisplayName("Ошибка, если не указана сумма (amount)")
    void validateShouldReturnErrorIfAmountIsNull() {
        Response response = new Response();
        response.setInternalId("internalId");
        response.setClientOrderId("clientOrderId");
        assertEquals("field \"amount\" must not be null", response.validate().errorsToString());
    }

    @Test
    void hasDetailsShouldReturnTrue() {
        assertTrue(new Response().hasDetails());
    }

}