package tgb.cryptoexchange.merchantdetails.details.neuralpay;

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
        response.setId("id");
        response.setAmount("0");
        Response.ResponseRequisite requisite = new Response.ResponseRequisite();
        response.setRequisite(requisite);
        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    @DisplayName("Ошибка, если отсутствует ID транзакции")
    void validateShouldReturnErrorIfIdIsNull() {
        Response response = new Response();
        response.setAmount("0");
        Response.ResponseRequisite requisite = new Response.ResponseRequisite();
        response.setRequisite(requisite);
        assertEquals("field \"id\" must not be null", response.validate().errorsToString());
    }

    @Test
    @DisplayName("Ошибка, если не указана сумма (amount)")
    void validateShouldReturnErrorIfAmountIsNull() {
        Response response = new Response();
        response.setId("id");
        Response.ResponseRequisite requisite = new Response.ResponseRequisite();
        response.setRequisite(requisite);
        assertEquals("field \"amount\" must not be null", response.validate().errorsToString());
    }

    @Test
    @DisplayName("Ошибка, если отсутствует объект реквизитов")
    void validateShouldReturnErrorIfRequisiteIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setAmount("0");
        assertEquals("field \"requisite\" must not be null", response.validate().errorsToString());
    }

    @Test
    void hasDetailsShouldReturnTrue() {
        assertTrue(new Response().hasDetails());
    }
}