package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.junit.jupiter.api.Test;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void validateShouldReturnNoErrorsIfDealsIsNull() {
        Response response = new Response();
        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void validateShouldReturnNoErrorsIfDealsIsEmpty() {
        Response response = new Response();
        response.setDeals(new ArrayList<>());
        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void validateShouldReturnErrorIfIdIsNull() {
        Response response = new Response();
        List<DealDTO> deals = new ArrayList<>();
        DealDTO dealDTO = new DealDTO();
        deals.add(dealDTO);
        RequisitesDTO requisitesDTO = new RequisitesDTO();
        requisitesDTO.setRequisites("requisites");
        dealDTO.setRequisites(requisitesDTO);
        dealDTO.setPaymentMethod(Bank.ABR);
        response.setDeals(deals);
        ValidationResult validationResult = response.validate();
        assertEquals("field \"id\" must not be null", validationResult.errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfDealIsNull() {
        Response response = new Response();
        response.setId("id");
        List<DealDTO> deals = new ArrayList<>();
        deals.add(null);
        RequisitesDTO requisitesDTO = new RequisitesDTO();
        requisitesDTO.setRequisites("requisites");
        response.setDeals(deals);
        ValidationResult validationResult = response.validate();
        assertEquals("field \"deal\" must not be null", validationResult.errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfDealPaymentMethodIsNull() {
        Response response = new Response();
        response.setId("id");
        List<DealDTO> deals = new ArrayList<>();
        DealDTO dealDTO = new DealDTO();
        deals.add(dealDTO);
        RequisitesDTO requisitesDTO = new RequisitesDTO();
        requisitesDTO.setRequisites("requisites");
        dealDTO.setRequisites(requisitesDTO);
        response.setDeals(deals);
        ValidationResult validationResult = response.validate();
        assertEquals("field \"deal.paymentMethod\" must not be null", validationResult.errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfDealRequisitesIsNull() {
        Response response = new Response();
        response.setId("id");
        List<DealDTO> deals = new ArrayList<>();
        DealDTO deal = new DealDTO();
        deals.add(deal);
        deal.setPaymentMethod(Bank.ABR);
        response.setDeals(deals);
        ValidationResult validationResult = response.validate();
        assertEquals("field \"deal.requisites\" must not be null", validationResult.errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfDealRequisitesRequisitesIsNull() {
        Response response = new Response();
        response.setId("id");
        List<DealDTO> deals = new ArrayList<>();
        DealDTO dealDTO = new DealDTO();
        deals.add(dealDTO);
        RequisitesDTO requisitesDTO = new RequisitesDTO();
        dealDTO.setRequisites(requisitesDTO);
        dealDTO.setPaymentMethod(Bank.ABR);
        response.setDeals(deals);
        ValidationResult validationResult = response.validate();
        assertEquals("field \"deal.requisites.requisites\" must not be null", validationResult.errorsToString());
    }

    @Test
    void hasDetailsShouldReturnFalseIfDealsIsNull() {
        Response response = new Response();
        assertFalse(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnFalseIfDealsIsEmpty() {
        Response response = new Response();
        response.setDeals(new ArrayList<>());
        assertFalse(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnTrueIfDealsNotEmpty() {
        Response response = new Response();
        response.setDeals(List.of(new DealDTO()));
        assertTrue(response.hasDetails());
    }
}