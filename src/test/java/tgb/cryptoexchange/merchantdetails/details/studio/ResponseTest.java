package tgb.cryptoexchange.merchantdetails.details.studio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ResponseTest {

    @Test
    @DisplayName("Успешный результат при заполнении всех обязательных полей")
    void validateShouldReturnNoErrorsIfAllFieldsPresent() {
        Response response = new Response();
        response.setInternalId("internalId");
        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    @DisplayName("Ошибка, если отсутствует internalId")
    void validateShouldReturnErrorIfInternalIdIsNull() {
        Response response = new Response();
        assertEquals("field \"internalId\" must not be null", response.validate().errorsToString());
    }

    @Test
    @DisplayName("Ошибка, если отсутствует requisites")
    void validateShouldReturnErrorIfRequisitesIsNull() {
        Response response = new Response();
        response.setInternalId("2332");
        response.setRequisites(new Response.Requisites());
        assertEquals("field \"bankName or bik\" must not be null", response.validate().errorsToString());
    }

    @Test
    void hasDetailsShouldReturnFalse() {
        assertFalse(new Response().hasDetails());
    }

}