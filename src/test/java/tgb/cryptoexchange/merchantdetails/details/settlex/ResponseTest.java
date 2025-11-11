package tgb.cryptoexchange.merchantdetails.details.settlex;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ResponseTest {

    @Test
    void validateShouldReturnNoErrorsIfAllFieldsPresent() {
        Response response = new Response();
        response.setId("id");
        response.setOrderId("orderId");
        response.setStatus(Status.CREATED);
        Response.Requisites requisites = new Response.Requisites();
        requisites.setBankName("bankName");
        requisites.setCardNumber("cardNumber");
        response.setRequisites(requisites);
        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void validateShouldReturnErrorIfIdIsNull() {
        Response response = new Response();
        response.setOrderId("orderId");
        response.setStatus(Status.CREATED);
        Response.Requisites requisites = new Response.Requisites();
        requisites.setBankName("bankName");
        requisites.setCardNumber("cardNumber");
        response.setRequisites(requisites);
        assertEquals("field \"id\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfOrderIdIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setStatus(Status.CREATED);
        Response.Requisites requisites = new Response.Requisites();
        requisites.setBankName("bankName");
        requisites.setCardNumber("cardNumber");
        response.setRequisites(requisites);
        assertEquals("field \"orderId\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfStatusIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setOrderId("orderId");
        Response.Requisites requisites = new Response.Requisites();
        requisites.setBankName("bankName");
        requisites.setCardNumber("cardNumber");
        response.setRequisites(requisites);
        assertEquals("field \"status\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfRequisitesIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setOrderId("orderId");
        response.setStatus(Status.CREATED);
        assertEquals("field \"requisites\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfBankNameIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setOrderId("orderId");
        response.setStatus(Status.CREATED);
        Response.Requisites requisites = new Response.Requisites();
        requisites.setCardNumber("cardNumber");
        response.setRequisites(requisites);
        assertEquals("field \"requisites.bankName\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfCardNumberIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setOrderId("orderId");
        response.setStatus(Status.CREATED);
        Response.Requisites requisites = new Response.Requisites();
        requisites.setBankName("bankName");
        response.setRequisites(requisites);
        assertEquals("field \"requisites.cardNumber\" must not be null", response.validate().errorsToString());
    }

    @Test
    void hasDetailsShouldReturnTrue() {
        assertTrue(new Response().hasDetails());
    }
}