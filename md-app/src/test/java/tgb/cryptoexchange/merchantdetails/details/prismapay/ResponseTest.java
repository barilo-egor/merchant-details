package tgb.cryptoexchange.merchantdetails.details.prismapay;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ResponseTest {

    @Test
    @DisplayName("Должен пройти валидацию, если статус 'success' и блок data отсутствует")
    void shouldPass_WhenStatusIsSuccess_AndDataIsNull() {
        Response response = new Response();
        response.setStatus("success");
        response.setData(null);

        ValidationResult result = response.validate();

        assertThat(result.isValid()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"success", "SUCCESS", "Success"})
    @DisplayName("Валидация статуса должна быть регистронезависимой")
    void shouldPass_WhenStatusIsSuccess_CaseInsensitive(String status) {
        Response response = new Response();
        response.setStatus(status);

        ValidationResult result = response.validate();

        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("Должен вернуть ошибку статуса, если статус не 'success'")
    void shouldFail_WhenStatusIsNotSuccess() {
        Response response = new Response();
        response.setStatus("failed");

        ValidationResult result = response.validate();

        assertThat(result.isValid()).isFalse();
    }

    @Test
    @DisplayName("Должен пропустить проверки деталей, если hasDetails() возвращает false (нет банка/реквизитов)")
    void shouldPass_WhenDataExists_ButHasDetailsIsFalse() {
        Response response = new Response();
        response.setStatus("success");

        Response.Data data = new Response.Data();
        data.setId(null);
        data.setAmount(null);
        data.setPaymentBank(null);
        response.setData(data);

        ValidationResult result = response.validate();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("Должен зафиксировать ошибки для id")
    void shouldFail_WhenRequiredIdIsNull() {
        Response response = new Response();
        response.setStatus("success");

        Response.Data data = new Response.Data();
        data.setPaymentBank("Sberbank");
        data.setPaymentDetails("12345678");
        data.setId(null);
        data.setAmount(2000);
        data.setStatus(Status.PENDING);
        response.setData(data);

        ValidationResult result = response.validate();

        assertThat(result.isValid()).isFalse();
        assertEquals("field \"data.id\" must not be null", response.validate().errorsToString());
    }

    @Test
    @DisplayName("Должен зафиксировать ошибки для amount")
    void shouldFail_WhenRequiredAmountIsNull() {
        Response response = new Response();
        response.setStatus("success");

        Response.Data data = new Response.Data();
        data.setPaymentBank("Sberbank");
        data.setPaymentDetails("12345678");
        data.setId("123");
        data.setAmount(null);
        data.setStatus(Status.PENDING);
        response.setData(data);

        ValidationResult result = response.validate();

        assertThat(result.isValid()).isFalse();
        assertEquals("field \"data.amount\" must not be null", response.validate().errorsToString());
    }

    @Test
    @DisplayName("Должен зафиксировать ошибки для status")
    void shouldFail_WhenRequiredStatusIsNull() {
        Response response = new Response();
        response.setStatus("success");

        Response.Data data = new Response.Data();
        data.setPaymentBank("Sberbank");
        data.setPaymentDetails("12345678");
        data.setId("123");
        data.setAmount(2000);
        data.setStatus(null);
        response.setData(data);

        ValidationResult result = response.validate();

        assertThat(result.isValid()).isFalse();
        assertEquals("field \"data.status\" must not be null", response.validate().errorsToString());
    }

    @Test
    @DisplayName("Должен успешно пройти валидацию со всеми заполненными деталями")
    void shouldPass_WhenAllDetailsAndRequiredFieldsAreValid() {
        Response response = new Response();
        response.setStatus("success");

        Response.Data data = new Response.Data();
        data.setPaymentBank("Tinkoff");
        data.setPaymentDetails("87654321");
        data.setId("123");
        data.setAmount(2000);
        data.setStatus(Status.PENDING);

        response.setData(data);

        ValidationResult result = response.validate();
        assertThat(result.isValid()).isTrue();
    }

}