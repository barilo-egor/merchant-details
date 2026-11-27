package net.rcetech.md.integrations.config;

import net.rcetech.md.meta.merchant.MerchantCatalog;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Инвариант: каждая группа конфига соответствует префиксу @ConfigurationProperties и наоборот.
 * Шаблон config_template/config.yml и фикстура integrations-test.yml должны покрывать один и тот же
 * набор префиксов — рассинхрон ловится здесь, а не в рантайме.
 */
class ConfigTemplateSyncTest {

    private static final Pattern PREFIX_PATTERN =
            Pattern.compile("@ConfigurationProperties\\((?:prefix\\s*=\\s*)?\"([a-z0-9-]+)\"");

    /** Инфраструктурные ключи шаблона, не являющиеся properties мерчантов/площадок. */
    private static final Set<String> INFRA_KEYS = Set.of(
            "server", "spring", "kafka", "callback-secret", "gateway-url", "details");

    private static final String PROPERTIES_PACKAGE_DIR =
            "src/main/java/net/rcetech/md/integrations/properties";

    @Test
    void everyPropertiesPrefixHasTemplateAndFixtureGroup() throws IOException {
        Set<String> prefixes = collectPrefixes();
        // Префиксы справочника мерчантов привязываются фабрикой по префиксу — группа нужна и им тоже.
        prefixes.addAll(java.util.Arrays.stream(MerchantCatalog.values())
                .map(MerchantCatalog::getConfigPrefix)
                .toList());

        Set<String> templateKeys = topLevelKeys(findRepoRoot().resolve("config_template/config.yml"));
        Set<String> fixtureKeys = topLevelKeys(Path.of("src/testFixtures/resources/integrations-test.yml"));

        Set<String> missingInTemplate = diff(prefixes, templateKeys);
        Set<String> missingInFixture = diff(prefixes, fixtureKeys);
        assertTrue(missingInTemplate.isEmpty(),
                "Префиксы без группы в config_template/config.yml (добавьте группу или удалите properties): " + missingInTemplate);
        assertTrue(missingInFixture.isEmpty(),
                "Префиксы без группы в integrations-test.yml: " + missingInFixture);
    }

    @Test
    void everyTemplateAndFixtureGroupHasPropertiesPrefix() throws IOException {
        Set<String> prefixes = collectPrefixes();
        prefixes.addAll(java.util.Arrays.stream(MerchantCatalog.values())
                .map(MerchantCatalog::getConfigPrefix)
                .toList());

        Set<String> templateKeys = topLevelKeys(findRepoRoot().resolve("config_template/config.yml"));
        Set<String> fixtureKeys = topLevelKeys(Path.of("src/testFixtures/resources/integrations-test.yml"));

        Set<String> deadInTemplate = diff(withoutInfra(templateKeys), prefixes);
        Set<String> deadInFixture = diff(withoutInfra(fixtureKeys), prefixes);
        assertTrue(deadInTemplate.isEmpty(),
                "Мёртвые группы в config_template/config.yml (нет @ConfigurationProperties с таким префиксом): " + deadInTemplate);
        assertTrue(deadInFixture.isEmpty(),
                "Мёртвые группы в integrations-test.yml: " + deadInFixture);
    }

    private Set<String> collectPrefixes() throws IOException {
        Set<String> prefixes = new LinkedHashSet<>();
        try (Stream<Path> files = Files.walk(Path.of(PROPERTIES_PACKAGE_DIR))) {
            List<Path> javaFiles = files
                    .filter(p -> p.toString().endsWith(".java"))
                    .toList();
            for (Path file : javaFiles) {
                Matcher matcher = PREFIX_PATTERN.matcher(Files.readString(file));
                while (matcher.find()) {
                    prefixes.add(matcher.group(1));
                }
            }
        }
        // Вариантные properties аккаунтов сносятся (аккаунт без собственного класса) — порог
        // проверяет только то, что пакет найден, а не конкретное количество префиксов.
        assertTrue(prefixes.size() > 10, "Похоже, пакет properties не найден: " + prefixes.size() + " префиксов");
        return prefixes;
    }

    private Set<String> withoutInfra(Set<String> keys) {
        Set<String> result = new LinkedHashSet<>(keys);
        result.removeAll(INFRA_KEYS);
        return result;
    }

    private Set<String> diff(Set<String> left, Set<String> right) {
        Set<String> result = new LinkedHashSet<>(left);
        result.removeAll(right);
        return result;
    }

    private Path findRepoRoot() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null && !Files.exists(dir.resolve("config_template/config.yml"))) {
            dir = dir.getParent();
        }
        assertTrue(dir != null, "Не найден корень репозитория (config_template/config.yml)");
        return dir;
    }

    private Set<String> topLevelKeys(Path yml) throws IOException {
        Set<String> keys = new LinkedHashSet<>();
        for (String line : Files.readAllLines(yml)) {
            if (line.matches("[a-z0-9-]+:.*")) {
                keys.add(line.substring(0, line.indexOf(':')));
            }
        }
        return keys;
    }
}
