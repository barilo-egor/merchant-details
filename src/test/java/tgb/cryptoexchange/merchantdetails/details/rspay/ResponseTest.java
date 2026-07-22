package tgb.cryptoexchange.merchantdetails.details.rspay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResponseTest {

    @Test
    void validateShouldReturnErrorInternalIdIdNull() {
        Response response = new Response();
        Response.Requisites data = new Response.Requisites();
        data.setBankName("bankName");
        data.setCardNumber("1111222233334444");
        response.setStatus(Status.AVAILABLE);
        response.setRequisites(data);

        assertEquals("field \"id\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorStatusNull() {
        Response response = new Response();
        Response.Requisites data = new Response.Requisites();
        data.setBankName("bankName");
        data.setCardNumber("1111222233334444");
        response.setId("1234");
        response.setRequisites(data);

        assertEquals("field \"status\" must not be null", response.validate().errorsToString());
    }

    @Test
    void hasDetailsShouldReturnTrueIfCard() {
        Response response = new Response();
        Response.Requisites data = new Response.Requisites();
        data.setBankName("bankName");
        data.setCardNumber("1111222233334444");
        response.setRequisites(data);
        assertTrue(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnTrueIfSim() {
        Response response = new Response();
        Response.Requisites data = new Response.Requisites();
        data.setMobileProvider("bankName");
        data.setPhoneNumber("88005553535");
        response.setRequisites(data);
        assertTrue(response.hasDetails());
    }

}
