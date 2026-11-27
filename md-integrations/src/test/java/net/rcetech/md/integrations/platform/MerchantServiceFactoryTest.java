package net.rcetech.md.integrations.platform;

import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.service.ReceiptService;
import net.rcetech.md.integrations.service.RequestService;
import net.rcetech.md.integrations.service.SignatureService;
import net.rcetech.md.integrations.service.SleepingService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;
import tgb.cryptoexchange.commons.enums.Merchant;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Фабрика создаёт сервисы через new вне Spring-контекста: @Autowired-сеттеры
 * базового {@link MerchantOrderCreationService} не срабатывают, зависимости
 * инжектит сама фабрика.
 * Регрессия: сервис EXTASY_PAY на сервере падал с NPE «objectMapper is null»
 * при создании ордера.
 */
class MerchantServiceFactoryTest {

    private MerchantServiceFactory factory(MockEnvironment environment) {
        return new MerchantServiceFactory(environment,
                mock(SignatureService.class), mock(CallbackConfig.class),
                mock(SleepingService.class), mock(ReceiptService.class),
                mock(RequestService.class), mock(ObjectMapper.class));
    }

    @Test
    void createdServiceShouldHaveObjectMapperAndRequestServiceInjected() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("extasy-pay.url", "https://api.example.com");
        environment.setProperty("extasy-pay.key", "key");
        environment.setProperty("extasy-pay.secret", "secret");

        Optional<MerchantOrderCreationService> service = factory(environment).create(Merchant.EXTASY_PAY);

        assertTrue(service.isPresent());
        assertNotNull(ReflectionTestUtils.getField(service.get(), "objectMapper"));
        assertNotNull(ReflectionTestUtils.getField(service.get(), "requestService"));
    }

    @Test
    void referencedUrlAccountsShouldHaveDependenciesInjectedToo() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("rs-pay-bt.apiKey", "key");
        environment.setProperty("rs-pay-bt.secret", "secret");
        environment.setProperty("rs-pay.url", "https://api.example.com");

        Optional<MerchantOrderCreationService> service = factory(environment).create(Merchant.RS_PAY_BT);

        assertTrue(service.isPresent());
        assertNotNull(ReflectionTestUtils.getField(service.get(), "objectMapper"));
        assertNotNull(ReflectionTestUtils.getField(service.get(), "requestService"));
    }

    @Test
    void optionalReturningBranchesShouldHaveDependenciesInjectedToo() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("lotrien.url", "https://api.example.com");

        Optional<MerchantOrderCreationService> service = factory(environment).create(Merchant.LOTRIEN_PDF);

        assertTrue(service.isPresent());
        assertNotNull(ReflectionTestUtils.getField(service.get(), "objectMapper"));
        assertNotNull(ReflectionTestUtils.getField(service.get(), "requestService"));
    }
}
