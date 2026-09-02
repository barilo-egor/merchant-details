package tgb.cryptoexchange.merchantdetails.details.tronex;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ResponseTest {

    @Test
    void validateShouldReturnNoErrorsIfStatusIsOkAndAllFieldsPresent() {
        Response response = new Response();
        response.setStatus("ok");
        response.setPlatform(createValidPlatformData());

        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void validateShouldReturnErrorIfStatusIsNotOk() {
        Response response = new Response();
        response.setStatus("error");
        response.setPlatform(createValidPlatformData());

        assertEquals("field \"status\" expected 'ok' but was error", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfStatusIsNull() {
        Response response = new Response();
        response.setStatus(null);
        response.setPlatform(createValidPlatformData());

        assertEquals("field \"status\" expected 'ok' but was null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfPlatformIdIsNull() {
        Response response = new Response();
        response.setStatus("ok");
        Response.Data platform = createValidPlatformData();
        platform.setId(null);
        response.setPlatform(platform);

        assertEquals("field \"platform.id\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfPlatformAmountIsNull() {
        Response response = new Response();
        response.setStatus("ok");
        Response.Data platform = createValidPlatformData();
        platform.setAmount(null);
        response.setPlatform(platform);

        assertEquals("field \"platform.amount\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfPlatformMethodIsNull() {
        Response response = new Response();
        response.setStatus("ok");
        Response.Data platform = createValidPlatformData();
        platform.setMethod(null);
        response.setPlatform(platform);

        assertEquals("field \"platform.method\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnNoErrorsIfPlatformIsNull() {
        Response response = new Response();
        response.setStatus("ok");
        response.setPlatform(null);

        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void hasDetailsShouldReturnTrueWhenBankAndNumberArePresent() {
        Response response = new Response();
        Response.Data platform = createValidPlatformData();
        platform.setPaymentUrl(null);
        response.setPlatform(platform);

        assertTrue(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnTrueWhenBankAndPaymentUrlArePresent() {
        Response response = new Response();
        Response.Data platform = createValidPlatformData();
        platform.setNumber(null);
        response.setPlatform(platform);

        assertTrue(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnFalseWhenPlatformIsNull() {
        Response response = new Response();
        response.setPlatform(null);

        assertFalse(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnFalseWhenBankIsNull() {
        Response response = new Response();
        Response.Data platform = createValidPlatformData();
        platform.setBank(null);
        response.setPlatform(platform);

        assertFalse(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnFalseWhenBothNumberAndPaymentUrlAreNull() {
        Response response = new Response();
        Response.Data platform = createValidPlatformData();
        platform.setNumber(null);
        platform.setPaymentUrl(null);
        response.setPlatform(platform);

        assertFalse(response.hasDetails());
    }

    private Response.Data createValidPlatformData() {
        Response.Data data = new Response.Data();
        data.setId("trade-12345");
        data.setMethod(Method.CARD);
        data.setAmount("1000.00");
        data.setBank("Tinkoff");
        data.setNumber("+79991234567");
        data.setPaymentUrl("https://pay.tronex.io/order/12345");
        return data;
    }
}