package tgb.cryptoexchange.merchantdetails.details;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Общий контрактный тест для проверки корректности заполнения реквизитов всеми мерчантами.
 * <p>
 * Гарантирует, что при добавлении нового мерчанта в enum {@link Merchant}:
 * 1. Зарегистрирован соответствующий {@link MerchantService} в {@link MerchantServiceRegistry}.
 * 2. Сервис заполняет либо {@code setQr(...)}, либо пару {@code setBank(...)} + {@code setDetails(...)}.
 * 3. Если вызывается {@code setDetails(...)}, то ОБЯЗАТЕЛЬНО должен быть вызван {@code setBank(...)}.
 */
@SpringBootTest
class MerchantRequisitesContractTest {

    /**
     * Мерчанты-исключения, которые на данный момент передают платёжную ссылку (URL)
     * в поле details без установки банка.
     */
    private static final Set<Merchant> EXCLUSIONS = Set.of(
            Merchant.PAYSCROW_TRANSGRAN,
            Merchant.SMACK_PAY
    );
    @Autowired
    private MerchantServiceRegistry merchantServiceRegistry;

    @ParameterizedTest(name = "[{0}] Requisites Contract: Bank+Details or QR")
    @EnumSource(Merchant.class)
    void checkMerchantRequisitesContract(Merchant merchant) throws IOException {
        Assumptions.assumeFalse(
                EXCLUSIONS.contains(merchant),
                () -> "Мерчант " + merchant + " находится в списке исключений"
        );

        Optional<MerchantService> serviceOpt = merchantServiceRegistry.getService(merchant);
        assertTrue(
                serviceOpt.isPresent(),
                () -> "Не найден зарегистрированный MerchantService для мерчанта: " + merchant
        );

        MerchantService service = serviceOpt.get();
        Set<String> detailsResponseCalls = getDetailsResponseCallsInHierarchy(service.getClass());

        boolean hasBank = detailsResponseCalls.contains("setBank");
        boolean hasDetails = detailsResponseCalls.contains("setDetails");
        boolean hasQr = detailsResponseCalls.contains("setQr");

        if (hasDetails) {
            assertTrue(
                    hasBank,
                    () -> String.format(
                            "Мерчант %s (%s) вызывает setDetails(), но не вызывает setBank()! " +
                                    "По стандарту банк должен передаваться отдельно через setBank.",
                            merchant, service.getClass().getSimpleName()
                    )
            );
        }

        assertTrue(
                hasQr || (hasBank && hasDetails),
                () -> String.format(
                        "Мерчант %s (%s) не устанавливает ни setQr(), ни пару (setBank() + setDetails()). Найденные вызовы DetailsResponse: %s",
                        merchant, service.getClass().getSimpleName(), detailsResponseCalls
                )
        );
    }

    private Set<String> getDetailsResponseCallsInHierarchy(Class<?> clazz) throws IOException {
        Set<String> allCalls = new HashSet<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            allCalls.addAll(inspectClassForDetailsResponseCalls(current));
            current = current.getSuperclass();
        }
        return allCalls;
    }

    private Set<String> inspectClassForDetailsResponseCalls(Class<?> clazz) throws IOException {
        String resourceName = clazz.getName().replace('.', '/') + ".class";
        try (InputStream is = clazz.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                return Collections.emptySet();
            }
            ClassReader reader = new ClassReader(is);
            DetailsResponseMethodCollector collector = new DetailsResponseMethodCollector();
            reader.accept(collector, 0);
            return collector.calls;
        }
    }

    private static class DetailsResponseMethodCollector extends ClassVisitor {
        private final Set<String> calls = new HashSet<>();

        DetailsResponseMethodCollector() {
            super(Opcodes.ASM9);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            return new MethodVisitor(Opcodes.ASM9) {
                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    if ("tgb/cryptoexchange/merchantdetails/details/DetailsResponse".equals(owner)) {
                        calls.add(name);
                    }
                }
            };
        }
    }
}
