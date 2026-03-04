package tgb.cryptoexchange.merchantdetails.details.yolo;

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
        response.setOrderId("orderId");
        response.setBankName("bankName");
        response.setAccountNumber("accountNumber");
        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    @DisplayName("Ошибка, если отсутствует orderId")
    void validateShouldReturnErrorIfOrderIdIsNull() {
        Response response = new Response();
        response.setBankName("bankName");
        response.setAccountNumber("accountNumber");
        assertEquals("field \"orderId\" must not be null", response.validate().errorsToString());
    }

    @Test
    @DisplayName("Ошибка, если отсутствует bankName")
    void validateShouldReturnErrorIfBankNameIsNull() {
        Response response = new Response();
        response.setOrderId("2332");
        response.setAccountNumber("accountNumber");
        assertEquals("field \"bankName\" must not be null", response.validate().errorsToString());
    }

    @Test
    @DisplayName("Ошибка, если отсутствует accountNumber")
    void validateShouldReturnErrorIfAccountNumberIsNull() {
        Response response = new Response();
        response.setOrderId("2332");
        response.setBankName("bankName");
        assertEquals("field \"accountNumber or contactNumber\" must not be null", response.validate().errorsToString());
    }

    @Test
    void hasDetailsShouldReturnTrue() {
        assertTrue(new Response().hasDetails());
    }

}