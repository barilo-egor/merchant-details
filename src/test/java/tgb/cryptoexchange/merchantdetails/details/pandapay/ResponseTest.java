package tgb.cryptoexchange.merchantdetails.details.pandapay;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void validateShouldReturnNoErrorsIfStatusIsTraderNotFound() {
        Response response = new Response();
        response.setStatus(Status.TRADER_NOT_FOUND);
        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void validateShouldReturnErrorIfStatusIsNull() {
        Response response = new Response();
        assertEquals("field \"status\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfUuidIsNull() {
        Response response = new Response();
        response.setStatus(Status.PENDING);
        Response.RequisiteData requisiteData = new Response.RequisiteData();
        requisiteData.setRequisites("requisites");
        requisiteData.setBank("bank");
        response.setRequisiteData(requisiteData);
        assertEquals("field \"uuid\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfRequisiteDataIsNull() {
        Response response = new Response();
        response.setStatus(Status.TIMEOUT);
        response.setUuid("uuid");
        assertEquals("field \"requisiteData\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfRequisiteDataRequisitesIsNull() {
        Response response = new Response();
        response.setUuid("uuid");
        response.setStatus(Status.COMPLETED);
        Response.RequisiteData requisiteData = new Response.RequisiteData();
        requisiteData.setBank("bank");
        response.setRequisiteData(requisiteData);
        assertEquals("field \"requisiteData.requisites\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfRequisiteDataBankIsNull() {
        Response response = new Response();
        response.setUuid("uuid");
        response.setStatus(Status.PENDING);
        Response.RequisiteData requisiteData = new Response.RequisiteData();
        requisiteData.setRequisites("requisites");
        response.setRequisiteData(requisiteData);
        assertEquals("field \"requisiteData.bank\" must not be null", response.validate().errorsToString());
    }

    @Test
    void hasDetailsShouldReturnTrueIfStatusIsNotTraderNotFound() {
        List<Status> statusList = List.of(Status.PENDING, Status.COMPLETED, Status.TIMEOUT);
        for (Status status : statusList) {
            Response response = new Response();
            response.setStatus(status);
            assertTrue(response.hasDetails());
        }
    }

    @Test
    void hasDetailsShouldReturnFalseIfStatusIsTraderNotFound() {
        Response response = new Response();
        response.setStatus(Status.TRADER_NOT_FOUND);
        assertFalse(response.hasDetails());
    }
}