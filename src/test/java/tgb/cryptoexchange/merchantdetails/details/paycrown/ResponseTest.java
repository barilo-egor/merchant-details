package tgb.cryptoexchange.merchantdetails.details.paycrown;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void validateShouldReturnNoErrorIfDataHasRequisites() {
        Response response = new Response();
        Response.Data data = new Response.Data();
        data.setId("id");
        Response.Data.Requisites requisites = new Response.Data.Requisites();
        data.setRequisites(requisites);
        requisites.setRequisitesString("requisites");
        requisites.setBank("bank");
        response.setData(data);
        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void validateShouldReturnNoErrorIfHasNoDetails() {
        Response response = new Response();
        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void validateShouldReturnErrorIfDataIdIsNull() {
        Response response = new Response();
        Response.Data data = new Response.Data();
        Response.Data.Requisites requisites = new Response.Data.Requisites();
        data.setRequisites(requisites);
        response.setData(data);
        requisites.setBank("bank");
        requisites.setRequisitesString("requisites");
        assertEquals("field \"data.id\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfDataRequisitesBankIsNull() {
        Response response = new Response();
        Response.Data data = new Response.Data();
        Response.Data.Requisites requisites = new Response.Data.Requisites();
        data.setRequisites(requisites);
        data.setId("id");
        response.setData(data);
        requisites.setRequisitesString("requisites");
        assertEquals("field \"data.requisites.bank\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfDataRequisitesRequisitesStringIsNull() {
        Response response = new Response();
        Response.Data data = new Response.Data();
        Response.Data.Requisites requisites = new Response.Data.Requisites();
        data.setRequisites(requisites);
        data.setId("id");
        response.setData(data);
        requisites.setBank("bank");
        assertEquals("field \"data.requisites.requisitesString\" must not be null", response.validate().errorsToString());
    }

    @Test
    void hasDetailsShouldReturnFalseIfDataIsNull() {
        Response response = new Response();
        assertFalse(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnFalseIfDataRequisitesIsNull() {
        Response response = new Response();
        response.setData(new Response.Data());
        assertFalse(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnTrueIfDataHasRequisites() {
        Response response = new Response();
        Response.Data data = new Response.Data();
        response.setData(data);
        Response.Data.Requisites requisites = new Response.Data.Requisites();
        data.setRequisites(requisites);
        assertTrue(response.hasDetails());
    }
}