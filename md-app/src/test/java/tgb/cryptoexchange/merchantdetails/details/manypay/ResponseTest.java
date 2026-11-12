package tgb.cryptoexchange.merchantdetails.details.manypay;

import org.junit.jupiter.api.Test;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ResponseTest {

    @Test
    void shouldFailWhenStatusIsNotOk() {
        Response response = new Response();
        response.setStatus("error");

        ValidationResult result = response.validate();

        assertThat(result.errorsToString())
                .contains("field \"status\"")
                .contains("expected 'ok' but was error");
    }

    @Test
    void shouldFailWhenStatusIsNull() {
        Response response = new Response();
        response.setStatus(null);

        ValidationResult result = response.validate();

        assertThat(result.errorsToString())
                .contains("field \"status\"")
                .contains("expected 'ok' but was null");
    }

    @Test
    void shouldFailWhenDataIsNull() {
        Response response = new Response();
        response.setStatus("ok");
        response.setData(null);

        ValidationResult result = response.validate();

        assertThat(result.errorsToString()).contains("field \"data\"");
    }

    @Test
    void shouldFailWhenOrderIdIsNull() {
        Response response = new Response();
        response.setStatus("ok");
        Response.Data data = new Response.Data();
        data.setOrderId(null);
        response.setData(data);

        ValidationResult result = response.validate();

        assertThat(result.errorsToString()).contains("field \"data.orderId\"");
    }

    @Test
    void shouldPassWhenStatusAndOrderIdAreValid() {
        Response response = new Response();
        response.setStatus("ok");
        Response.Data data = new Response.Data();
        data.setOrderId("12345");
        response.setData(data);

        ValidationResult result = response.validate();

        assertThat(result.errorsToString()).isEmpty();
    }

    @Test
    void shouldReturnFalseWhenDataIsNull() {
        Response response = new Response();
        response.setData(null);

        assertThat(response.hasDetails()).isFalse();
    }

    @Test
    void shouldReturnFalseWhenPaymentDetailsIsNull() {
        Response response = new Response();
        Response.Data data = new Response.Data();
        data.setPaymentDetails(null);
        response.setData(data);

        assertThat(response.hasDetails()).isFalse();
    }

    @Test
    void shouldReturnFalseWhenBankNameIsNull() {
        Response response = new Response();
        Response.Data data = new Response.Data();
        Response.Requisites requisites = new Response.Requisites();
        requisites.setBankName(null);
        requisites.setMethod(Method.CARD);
        requisites.setDetails("123456789");
        data.setPaymentDetails(requisites);
        response.setData(data);

        assertThat(response.hasDetails()).isFalse();
    }

    @Test
    void shouldReturnFalseWhenMethodIsNull() {
        Response response = new Response();
        Response.Data data = new Response.Data();
        Response.Requisites requisites = new Response.Requisites();
        requisites.setBankName("SomeBank");
        requisites.setMethod(null);
        requisites.setDetails("123456789");
        data.setPaymentDetails(requisites);
        response.setData(data);

        assertThat(response.hasDetails()).isFalse();
    }

    @Test
    void shouldReturnFalseWhenDetailsIsNull() {
        Response response = new Response();
        Response.Data data = new Response.Data();
        Response.Requisites requisites = new Response.Requisites();
        requisites.setBankName("SomeBank");
        requisites.setMethod(Method.CARD);
        requisites.setDetails(null);
        data.setPaymentDetails(requisites);
        response.setData(data);

        assertThat(response.hasDetails()).isFalse();
    }

    @Test
    void shouldReturnTrueWhenAllDetailsArePresent() {
        Response response = new Response();
        Response.Data data = new Response.Data();
        Response.Requisites requisites = new Response.Requisites();
        requisites.setBankName("SomeBank");
        requisites.setMethod(Method.CARD);
        requisites.setDetails("123456789");
        data.setPaymentDetails(requisites);
        response.setData(data);

        assertThat(response.hasDetails()).isTrue();
    }

}