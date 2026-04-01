package tgb.cryptoexchange.merchantdetails.details.cashout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ResponseTest {

    @Test
    void validate_ShouldReturnError_WhenDataIsNull() {
        Response response = new Response();
        response.setData(null);

        ValidationResult result = response.validate();

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void validate_ShouldReturnErrors_WhenInnerFieldsAreNull() {
        Response response = new Response();
        Response.ResponseRequisite requisite = new Response.ResponseRequisite();
        response.setData(requisite);

        ValidationResult result = response.validate();

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void validate_ShouldBeValid_WhenAllFieldsPresent() {
        Response response = new Response();
        Response.ResponseRequisite requisite = new Response.ResponseRequisite();
        requisite.setTransactionId("TX-123");
        requisite.setAmount("1000");
        requisite.setStatus(Status.CANCELLED);
        response.setData(requisite);

        ValidationResult result = response.validate();

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void hasDetails_ShouldReturnFalse_WhenPaymentDetailsIsNull() {
        Response response = new Response();
        Response.ResponseRequisite requisite = new Response.ResponseRequisite();
        requisite.setPaymentDetails(null);
        response.setData(requisite);

        assertThat(response.hasDetails()).isFalse();
    }

    @Test
    void hasDetails_ShouldReturnFalse_WhenFieldsInPaymentDetailsAreNull() {
        Response response = new Response();
        Response.ResponseRequisite requisite = new Response.ResponseRequisite();
        Response.PaymentDetails details = new Response.PaymentDetails();

        details.setCardNumber("1234");
        details.setBankName(null);
        requisite.setPaymentDetails(details);
        response.setData(requisite);
        assertThat(response.hasDetails()).isFalse();

        details.setCardNumber(null);
        details.setBankName("Sber");
        assertThat(response.hasDetails()).isFalse();
    }

    @Test
    void hasDetails_ShouldReturnTrue_WhenAllDetailsPresent() {
        Response response = new Response();
        Response.ResponseRequisite requisite = new Response.ResponseRequisite();
        Response.PaymentDetails details = new Response.PaymentDetails();
        details.setCardNumber("44445555");
        details.setBankName("Alfa");

        requisite.setPaymentDetails(details);
        response.setData(requisite);

        assertThat(response.hasDetails()).isTrue();
    }

    @Test
    void lombok_ShouldWorkCorrectly() {
        Response r1 = new Response();
        r1.setSuccess("true");

        Response r2 = new Response();
        r2.setSuccess("true");

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        assertThat(r1.toString()).contains("success=true");
    }
}