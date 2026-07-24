package tgb.cryptoexchange.merchantdetails.details.eclipsegate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ResponseTest {

    @Test
    @DisplayName("Должен возвращать false, если requisites равен null")
    void shouldReturnFalseWhenRequisitesIsNull() {
        Response response = new Response();
        response.setRequisites(null);
        assertThat(response.hasDetails()).isFalse();
    }

    @Test
    @DisplayName("Должен возвращать false, если requisites не null, но bill и phone равны null")
    void shouldReturnFalseWhenRequisitesIsEmpty() {
        Response response = new Response();
        Response.Requisites requisites = new Response.Requisites();
        requisites.setBankName("Bank");
        response.setRequisites(requisites);

        assertThat(response.hasDetails()).isFalse();
    }

    @Test
    @DisplayName("Должен возвращать false, если заполнен только bankName")
    void shouldReturnTrueWhenBankNameIsPresent() {
        Response response = new Response();
        Response.Requisites requisites = new Response.Requisites();
        requisites.setBankName("Sberbank");
        response.setRequisites(requisites);

        assertThat(response.hasDetails()).isFalse();
    }

    @Test
    @DisplayName("Должен возвращать true, если заполнен только bankName и phone")
    void shouldReturnTrueWhenPhoneIsPresent() {
        Response response = new Response();
        Response.Requisites requisites = new Response.Requisites();
        requisites.setBankName("Sberbank");
        requisites.setBill("+79991112233");
        response.setRequisites(requisites);

        assertThat(response.hasDetails()).isTrue();
    }

    @Test
    @DisplayName("Должен возвращать true, если заполнен только bankName и bill")
    void shouldReturnTrueWhenBillIsPresent() {
        Response response = new Response();
        Response.Requisites requisites = new Response.Requisites();
        requisites.setBankName("Sberbank");
        requisites.setBill("1111 2222 3333 4444");
        response.setRequisites(requisites);

        assertThat(response.hasDetails()).isTrue();
    }

    @Test
    @DisplayName("Должен возвращать пустой ValidationResult, если hasDetails() равен false")
    void shouldNotValidateFieldsWhenNoDetails() {
        Response response = new Response();
        ValidationResult result = response.validate();

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Должен регистрировать ошибку orderId")
    void shouldCollectErrorsWhenOrderIdIsNull() {
        Response response = new Response();
        response.setStatus(Status.ERROR);
        response.setAmount(1000);
        Response.Requisites requisites = new Response.Requisites();
        requisites.setBankName("Alfa");
        requisites.setBill("88005553535");
        response.setRequisites(requisites);

        assertEquals("field \"orderId\" must not be null", response.validate().errorsToString());
    }

    @Test
    @DisplayName("Должен регистрировать ошибку amount")
    void shouldCollectErrorsWhenAmountIsNull() {
        Response response = new Response();
        response.setStatus(Status.CANCELLED);
        response.setOrderId("qwe");
        Response.Requisites requisites = new Response.Requisites();
        requisites.setBankName("Alfa");
        requisites.setBill("88005553535");
        response.setRequisites(requisites);

        assertEquals("field \"amount\" must not be null", response.validate().errorsToString());
    }

    @Test
    @DisplayName("Должен регистрировать ошибку status")
    void shouldCollectErrorsWhenStatusIsNull() {
        Response response = new Response();
        response.setAmount(1000);
        response.setOrderId("qwe");
        Response.Requisites requisites = new Response.Requisites();
        requisites.setBankName("Alfa");
        requisites.setBill("88005553535");
        response.setRequisites(requisites);

        assertEquals("field \"status\" must not be null", response.validate().errorsToString());
    }

}