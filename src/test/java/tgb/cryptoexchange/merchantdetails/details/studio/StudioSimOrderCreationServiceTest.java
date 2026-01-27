package tgb.cryptoexchange.merchantdetails.details.studio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.StudioConfig;
import tgb.cryptoexchange.merchantdetails.properties.StudioProperties;
import tgb.cryptoexchange.merchantdetails.properties.StudioSimProperties;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StudioSimOrderCreationServiceTest {

    @Mock
    private StudioConfig properties;

    @Mock
    private DetailsRequest detailsRequest;

    @InjectMocks
    private StudioSimOrderCreationService service;

    @Test
    @DisplayName("Проверка заголовков для Sim")
    void shouldAddSimHeaderWhenMethodIsSim() {
        when(properties.getKey("SIM")).thenReturn("key-for-sim");
        when(detailsRequest.getMerchantMethod(Merchant.STUDIO_SIM)).thenReturn(Optional.of("SIM"));
        HttpHeaders headers = new HttpHeaders();

        Consumer<HttpHeaders> consumer = service.headers(detailsRequest, "some body");
        consumer.accept(headers);

        assertEquals("application/json", headers.getFirst("Content-Type"));
        assertEquals("key-for-sim", headers.getFirst("X-API-Key"));
    }


    @Test
    void getMerchantShouldReturnStudioSim() {
        assertEquals(Merchant.STUDIO_SIM, service.getMerchant());
    }

}

