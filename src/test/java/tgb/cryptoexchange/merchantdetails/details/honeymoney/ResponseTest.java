package tgb.cryptoexchange.merchantdetails.details.honeymoney;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ResponseTest {

    @Test
    void validateShouldReturnErrorIfIdIsNull() {
        Response response = new Response();
        response.setCardNumber("cardNumber");
        response.setBankName("bankName");
        assertEquals("field \"id\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfCardNumberAndPhoneNumberIsNull() {
        Response response = new Response();
        response.setId(1);
        response.setBankName("bankName");
        assertEquals("field \"cardNumber or phoneNumber\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfBankNameIsNull() {
        Response response = new Response();
        response.setId(1);
        response.setPhoneNumber("cardNumber");
        assertEquals("field \"bankName\" must not be null", response.validate().errorsToString());
    }

    @Test
    void hasDetailsShouldReturnTrue() {
        Response response = new Response();
        assertTrue(response.hasDetails());
    }
}