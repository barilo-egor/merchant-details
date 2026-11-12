package tgb.cryptoexchange.merchantdetails.details.wat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ResponseTest {

    @Test
    void validateShouldReturnNoErrorsIfAllFieldsPresent() {
        Response response = new Response();
        response.setData(createValidData());

        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void validateShouldReturnErrorIfIdIsNull() {
        Response response = new Response();
        Response.Data data = createValidData();
        data.setId(null);
        response.setData(data);

        assertEquals("field \"data.id\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfRequisiteIsNull() {
        Response response = new Response();
        Response.Data data = createValidData();
        data.setRequisite(null);
        response.setData(data);

        assertEquals("field \"data.requisite\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfStatusIsNull() {
        Response response = new Response();
        Response.Data data = createValidData();
        data.setStatus(null);
        response.setData(data);

        assertEquals("field \"data.status\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnNoErrorsIfHasNoDetails() {
        Response response = new Response();
        Response.Data data = new Response.Data();
        response.setData(data);

        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void hasDetailsShouldReturnTrueWhenBankAndNameArePresent() {
        Response response = new Response();
        response.setData(createValidData());

        assertTrue(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnFalseWhenBankIsNull() {
        Response response = new Response();
        Response.Data data = new Response.Data();
        response.setData(data);

        assertFalse(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnFalseWhenBankNameIsNull() {
        Response response = new Response();
        Response.Data data = new Response.Data();
        data.setBank(new Response.Data.BankInfo());
        response.setData(data);

        assertFalse(response.hasDetails());
    }

    private Response.Data createValidData() {
        Response.Data data = new Response.Data();
        data.setId("order-wat-123");
        data.setRequisite("+79991234567");
        data.setStatus(Status.AWAITING_PAYMENT);

        Response.Data.BankInfo bankInfo = new Response.Data.BankInfo();
        bankInfo.setName("Sberbank");
        data.setBank(bankInfo);

        return data;
    }

}