package tgb.cryptoexchange.merchantdetails.details.cube;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResponseTest {

    @Test
    void validateShouldReturnErrorInternalIdIdNull() {
        Response response = new Response();
        Response.Data data = new Response.Data();
        data.setBankName("bankName");
        data.setReceiver("receiver");
        data.setStatus(Status.ACCEPTED);
        data.setAmount(Double.valueOf("100"));
        response.setData(data);

        assertEquals("field \"data.internalId\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorStatusNull() {
        Response response = new Response();
        Response.Data data = new Response.Data();
        data.setBankName("bankName");
        data.setReceiver("receiver");
        data.setInternalId("internalId");
        data.setAmount(Double.valueOf("100"));
        response.setData(data);

        assertEquals("field \"data.status\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorAmountNull() {
        Response response = new Response();
        Response.Data data = new Response.Data();
        data.setBankName("bankName");
        data.setReceiver("receiver");
        data.setInternalId("internalId");
        data.setStatus(Status.APPEAL);
        response.setData(data);

        assertEquals("field \"data.amount\" must not be null", response.validate().errorsToString());
    }


    @Test
    void hasDetailsShouldReturnTrue() {
        Response response = new Response();
        Response.Data data = new Response.Data();
        data.setBankName("bankName");
        data.setReceiver("receiver");
        response.setData(data);
        assertTrue(response.hasDetails());
    }
}