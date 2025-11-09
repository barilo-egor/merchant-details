package tgb.cryptoexchange.merchantdetails.details.daopayments;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ResponseTest {

    @Test
    void validateShouldReturnErrorIfTransactionIdIsNull() {
        Response response = new Response();
        response.setStatus(Status.PENDING);
        Response.TransferDetails transferDetails = new Response.TransferDetails();
        transferDetails.setBankName("bankName");
        transferDetails.setCardNumber("cardNumber");
        response.setTransferDetails(transferDetails);
        ValidationResult actual = response.validate();
        assertEquals("field \"transactionId\" must not be null", actual.errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfStatusIsNull() {
        Response response = new Response();
        response.setTransactionId("transactionId");
        Response.TransferDetails transferDetails = new Response.TransferDetails();
        transferDetails.setBankName("bankName");
        transferDetails.setCardNumber("cardNumber");
        response.setTransferDetails(transferDetails);
        ValidationResult actual = response.validate();
        assertEquals("field \"status\" must not be null", actual.errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfTransferDetailsIsNull() {
        Response response = new Response();
        response.setTransactionId("transactionId");
        response.setStatus(Status.PENDING);
        ValidationResult actual = response.validate();
        assertEquals("field \"transferDetails\" must not be null", actual.errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfTransferDetailsBankNameIsNull() {
        Response response = new Response();
        response.setTransactionId("transactionId");
        response.setStatus(Status.PENDING);
        Response.TransferDetails transferDetails = new Response.TransferDetails();
        transferDetails.setCardNumber("cardNumber");
        response.setTransferDetails(transferDetails);
        ValidationResult actual = response.validate();
        assertEquals("field \"transferDetails.bankName\" must not be null", actual.errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfTransferDetailsCardNumberIsNull() {
        Response response = new Response();
        response.setTransactionId("transactionId");
        response.setStatus(Status.PENDING);
        Response.TransferDetails transferDetails = new Response.TransferDetails();
        transferDetails.setBankName("bankName");
        response.setTransferDetails(transferDetails);
        ValidationResult actual = response.validate();
        assertEquals("field \"transferDetails.cardNumber\" must not be null", actual.errorsToString());
    }

    @Test
    void hasDetailsShouldReturnTrue() {
        Response response = new Response();
        assertTrue(response.hasDetails());
    }
}