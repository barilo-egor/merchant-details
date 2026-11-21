package net.rcetech.md.callback.callback;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.platform.CallbackStatus;
import net.rcetech.md.integrations.platform.MerchantService;
import net.rcetech.md.integrations.platform.MerchantServiceRegistry;
import net.rcetech.md.meta.exception.ServiceUnavailableException;
import net.rcetech.md.callback.kafka.MerchantCallbackEvent;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantCallbackServiceTest {

    private static final String BODY = "{\"id\":\"order-1\"}";

    private static final String TOPIC = "merchant-details-callback";

    @Mock
    private MerchantServiceRegistry merchantServiceRegistry;

    @Mock
    private Environment environment;

    @Mock
    private KafkaTemplate<String, MerchantCallbackEvent> callbackKafkaTemplate;

    @Mock
    private MerchantService merchantService;

    private MerchantCallbackService service;

    @BeforeEach
    void setUp() {
        service = new MerchantCallbackService(merchantServiceRegistry, environment);
        service.setCallbackKafkaTemplate(callbackKafkaTemplate);
    }

    @Test
    void updateStatusShouldSendParsedStatusAndThenDeleteReceipt() {
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(merchantService));
        when(merchantService.parseCallback(BODY)).thenReturn(Optional.of(new CallbackStatus("order-1", "PAID", "Оплачено")));
        when(environment.getRequiredProperty("kafka.topic.merchant-details.callback")).thenReturn(TOPIC);

        service.updateStatus(Merchant.ALFA_TEAM, BODY);

        ArgumentCaptor<MerchantCallbackEvent> eventCaptor = ArgumentCaptor.forClass(MerchantCallbackEvent.class);
        InOrder inOrder = inOrder(callbackKafkaTemplate, merchantService);
        inOrder.verify(callbackKafkaTemplate).send(eq(TOPIC), eq("ALFA_TEAM_order-1"), eventCaptor.capture());
        inOrder.verify(merchantService).deleteReceipt("order-1", "PAID");
        MerchantCallbackEvent event = eventCaptor.getValue();
        assertAll(
                () -> assertEquals("order-1", event.getMerchantOrderId()),
                () -> assertEquals("PAID", event.getStatus()),
                () -> assertEquals("Оплачено", event.getStatusDescription()),
                () -> assertEquals(Merchant.ALFA_TEAM, event.getMerchant())
        );
    }

    @Test
    void updateStatusShouldDoNothingIfCallbackHasNoStatus() {
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(merchantService));
        when(merchantService.parseCallback(BODY)).thenReturn(Optional.empty());

        service.updateStatus(Merchant.ALFA_TEAM, BODY);

        verifyNoInteractions(callbackKafkaTemplate);
        verify(merchantService, never()).deleteReceipt(any(), any());
    }

    @Test
    void updateStatusShouldDoNothingIfMerchantHasNoImplementation() {
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.updateStatus(Merchant.ALFA_TEAM, BODY));

        verifyNoInteractions(callbackKafkaTemplate);
    }

    @Test
    void updateStatusShouldThrowAndKeepReceiptIfKafkaSendFails() {
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(merchantService));
        when(merchantService.parseCallback(BODY)).thenReturn(Optional.of(new CallbackStatus("order-1", "PAID", "Оплачено")));
        when(environment.getRequiredProperty(anyString())).thenReturn(TOPIC);
        when(callbackKafkaTemplate.send(anyString(), anyString(), any())).thenThrow(new RuntimeException("Kafka error"));

        assertThrows(ServiceUnavailableException.class, () -> service.updateStatus(Merchant.ALFA_TEAM, BODY));

        verify(merchantService, never()).deleteReceipt(any(), any());
    }

    @Test
    void updateStatusShouldDeleteReceiptWithoutSendingIfKafkaIsDisabled() {
        MerchantCallbackService serviceWithoutKafka = new MerchantCallbackService(merchantServiceRegistry, environment);
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(merchantService));
        when(merchantService.parseCallback(BODY)).thenReturn(Optional.of(new CallbackStatus("order-1", "PAID", "Оплачено")));

        serviceWithoutKafka.updateStatus(Merchant.ALFA_TEAM, BODY);

        verify(merchantService).deleteReceipt("order-1", "PAID");
        verifyNoInteractions(environment);
    }

    @Test
    void fetchStatusShouldUpdateStatusByFetchedCallbackData() {
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(merchantService));
        when(merchantService.fetchCallbackDataByTransactionId("tx-1")).thenReturn(Optional.of(BODY));
        when(merchantService.parseCallback(BODY)).thenReturn(Optional.empty());

        service.fetchStatus(Merchant.ALFA_TEAM, "tx-1");

        verify(merchantService).parseCallback(BODY);
    }

    @Test
    void fetchStatusShouldNotUpdateStatusIfMerchantReturnedNoData() {
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(merchantService));
        when(merchantService.fetchCallbackDataByTransactionId("tx-1")).thenReturn(Optional.empty());

        service.fetchStatus(Merchant.ALFA_TEAM, "tx-1");

        verify(merchantService, never()).parseCallback(any());
    }
}
