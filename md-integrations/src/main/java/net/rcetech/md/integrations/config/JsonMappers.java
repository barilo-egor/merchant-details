package net.rcetech.md.integrations.config;

import tools.jackson.databind.json.JsonMapper;

/**
 * Единственная точка создания {@link JsonMapper} в проекте.
 * <p>
 * Мапперы создаются с настройками по умолчанию Jackson 2, чтобы формат JSON
 * в Kafka, запросах к мерчантам и REST не зависел от изменённых дефолтов Jackson 3
 * (даты числом, порядок полей как в классе, enum по имени).
 * Создавать мапперы напрямую через {@code new ObjectMapper()} или {@code JsonMapper.builder()} запрещено,
 * это проверяет {@code JsonMappersArchTest}. Там, где доступен Spring-контекст, нужно внедрять бин из {@link CommonConfig}.
 */
public final class JsonMappers {

    private JsonMappers() {
    }

    /**
     * Возвращает билдер с настройками по умолчанию Jackson 2 для маппера с дополнительными настройками.
     *
     * @return билдер маппера
     */
    public static JsonMapper.Builder builder() {
        return JsonMapper.builderWithJackson2Defaults();
    }

    /**
     * Создаёт маппер с настройками по умолчанию Jackson 2.
     *
     * @return маппер
     */
    public static JsonMapper create() {
        return builder().build();
    }
}
