package tgb.cryptoexchange.merchantdetails.details.appexbit;

import org.junit.jupiter.api.Test;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void validateShouldReturnSuccessIsNull() {
        Response response = new Response();
        ValidationResult validationResult = response.validate();
        assertAll(
                () -> assertEquals("field \"success\" must not be null", validationResult.errorsToString())
        );
    }

    @Test
    void validateShouldReturnSuccessIsFalse() {
        Response response = new Response();
        response.setSuccess(false);
        ValidationResult validationResult = response.validate();
        assertAll(
                () -> assertEquals("field \"success\" expected true but was false", validationResult.errorsToString())
        );
    }

    @Test
    void validateShouldReturnValidIfSuccessIsTrueAndHasNoDetails() {
        Response response = new Response();
        response.setSuccess(true);
        response.setAddedOffers(new ArrayList<>());
        ValidationResult validationResult = response.validate();
        assertAll(
                () -> assertTrue(validationResult.errorsToString().isEmpty())
        );
    }

    @Test
    void validateShouldReturnValidIfSuccessIsTrueAndAddedOffersSizeMoreThan1() {
        Response response = new Response();
        response.setSuccess(true);
        List<Response.Offer> offers = new ArrayList<>();
        offers.add(new Response.Offer());
        offers.add(new Response.Offer());
        response.setAddedOffers(offers);
        ValidationResult validationResult = response.validate();
        assertAll(
                () -> assertEquals("field \"addedOffers\" size expected <= 1", validationResult.errorsToString())
        );
    }

    @Test
    void validateShouldReturnInvalidIfOfferIsNull() {
        Response response = new Response();
        response.setSuccess(true);
        List<Response.Offer> offers = new ArrayList<>();
        offers.add(null);
        response.setAddedOffers(offers);
        ValidationResult validationResult = response.validate();
        assertAll(
                () -> assertEquals("field \"offer\" must not be null", validationResult.errorsToString())
        );
    }

    @Test
    void validateShouldReturnInvalidIfOfferIdIsNull() {
        Response response = new Response();
        response.setSuccess(true);
        List<Response.Offer> offers = new ArrayList<>();
        Response.Offer offer = new Response.Offer();
        offer.setStatus(Status.DISPUTE);
        offers.add(offer);
        response.setAddedOffers(offers);
        ValidationResult validationResult = response.validate();
        assertAll(
                () -> assertEquals("field \"offer.id\" must not be null", validationResult.errorsToString())
        );
    }

    @Test
    void validateShouldReturnInvalidIfOfferStatusIsNull() {
        Response response = new Response();
        response.setSuccess(true);
        List<Response.Offer> offers = new ArrayList<>();
        Response.Offer offer = new Response.Offer();
        offer.setId("id");
        offers.add(offer);
        response.setAddedOffers(offers);
        ValidationResult validationResult = response.validate();
        assertAll(
                () -> assertEquals("field \"offer.status\" must not be null", validationResult.errorsToString())
        );
    }

    @Test
    void validateShouldReturnInvalidIfIdAndStatusIsNull() {
        Response response = new Response();
        response.setSuccess(true);
        List<Response.Offer> offers = new ArrayList<>();
        Response.Offer offer = new Response.Offer();
        offers.add(offer);
        response.setAddedOffers(offers);
        ValidationResult validationResult = response.validate();
        assertAll(
                () -> assertEquals("field \"offer.id\" must not be null;field \"offer.status\" must not be null", validationResult.errorsToString())
        );
    }

    @Test
    void hasDetailsShouldReturnTrueIfAddedOffersHasOffer() {
        Response response = new Response();
        response.setAddedOffers(List.of(new Response.Offer()));
        assertTrue(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnFalseIfAddedOffersIsNull() {
        Response response = new Response();
        assertFalse(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnFalseIfAddedOffersIsEmpty() {
        Response response = new Response();
        response.setAddedOffers(new ArrayList<>());
        assertFalse(response.hasDetails());
    }
}