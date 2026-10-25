# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Сборка и тесты

Проект на Gradle (wrapper), Java 21, Spring Boot 3.5.

```bash
./gradlew build                       # сборка + тесты
./gradlew test                        # все тесты
./gradlew test --tests 'tgb.cryptoexchange.merchantdetails.details.asgard.AsgardSimOrderCreationServiceTest'
./gradlew test --tests '*AsgardSim*'  # по маске
./gradlew bootJar                     # build/libs/merchant-details.jar
./gradlew generateProto               # перегенерировать gRPC-классы из src/main/proto
```

CI (Jenkinsfile) запускает `./gradlew clean test bootJar --no-daemon`, затем копирует jar на сервер и делает `docker rollout merchant-details`. Падение тестов блокирует деплой.

Зависимости `tgb.cryptoexchange:commons` и `tgb.cryptoexchange:common-api` берутся из `mavenLocal()`: без них в `~/.m2` проект не соберётся. Enum `Merchant` (`tgb.cryptoexchange.commons.enums.Merchant`) живёт в `commons`, поэтому новый мерчант сначала добавляется туда.

## Конфигурация и запуск

- `src/main/resources/application.yml` импортирует `file:config/config.yml`. Каталог `config/` в `.gitignore`; шаблон с пустыми значениями лежит в `config_template/config.yml`.
- `src/test/resources/application.yml` содержит фиктивные значения для каждого мерчанта (`<prefix>-url`, `<prefix>-token` и т. п.), H2 в памяти и отключённый Spring Cloud Config. Тесты `properties/*PropertiesTest` сверяют именно эти значения.
- Профиль `kafka-disabled` отключает Kafka-листенеры, продюсеры и бины `ObjectMapper`/`KafkaTemplate` из `CommonConfig`; поэтому зависимости от Kafka в сервисах внедряются с `@Autowired(required = false)` и проверяются на `null`.
- Профиль `clear-merchants` при старте удаляет `MerchantConfig` мерчантов, которых больше нет в enum, и пересчитывает порядок.
- `server.servlet.context-path` = `/merchant-details`.

## Архитектура

Сервис получает реквизиты для сделок обменника у внешних платёжных мерчантов, принимает от них callback'и и хранит настройки мерчантов в MySQL (JPA).

### Входные точки

- Kafka: `DetailsRequestTopicListener` (топик `kafka.topic.merchant-details.request`) → `DetailsRequestProcessorService` запускает поиск в пуле `detailsRequestSearchExecutor` и хранит `Future` в `activeSearchMap` по id сделки (повторный запрос по той же сделке игнорируется). Результат (или пустой `DetailsResponse`) уходит в топик `...response`. `StopDetailsSearchTopicListener` (`...stop-search`) отменяет поиск через `Future.cancel(true)`, поэтому код поиска проверяет `Thread.currentThread().isInterrupted()`.
- REST `MerchantDetailsController`: конфиги мерчантов, переменные, загрузка чеков.
- REST `MerchantCallbackController` (`/merchant-details/callback?merchant=...&secret=...`): callback'и мерчантов, проверяются по `callback-secret`. Для части мерчантов тело сначала расшифровывается через `CallbackDecryptService` (map собирается в `CommonConfig`).
- gRPC `MerchantDetailsControllerGrpc` реализует `src/main/proto/merchantdetails.proto` (сгенерированные классы в пакете `tgb.cryptoexchange.grpc.generated`) и во многом дублирует REST-контроллер.

### Поиск реквизитов

`MerchantDetailsService.getDetails(request)`:
1. `MerchantConfigService.findAllByMethodsAndAmount` выбирает включённые мерчанты, подходящие по методам и сумме, в порядке `merchantOrder`.
2. Мерчанты перебираются по очереди; цикл повторяется `VariableType.ATTEMPTS_COUNT` раз с минимальной длительностью попытки `MIN_ATTEMPT_TIME` (переменные хранятся в БД, `VariableService`).
3. Для каждого мерчанта перебираются его методы из запроса; первый непустой ответ возвращается и публикуется событием `MerchantDetailsReceiveEvent`.
4. Ошибки отдельного мерчанта логируются и считаются в метриках (`constants/Metrics`, Micrometer/Prometheus), поиск продолжается.

### Как устроен мерчант

Каждая интеграция — Spring-бин, реализующий `MerchantService`; `MerchantServiceRegistry` собирает их в `EnumMap<Merchant, MerchantService>` по `getMerchant()`. Почти все наследуют шаблонный `MerchantOrderCreationService<Response, Callback>`, который реализует весь поток (маппинг тела, запрос через `RequestService`, разбор и `validate()` ответа, разбор callback'а и отправку `MerchantCallbackEvent` в Kafka). Наследник переопределяет `uriBuilder`, `headers`, `body`, `buildResponse` и при необходимости хуки: `method()`, `isValidRequestPredicate`, `isNoDetailsExceptionPredicate`, `hasResponseNoDetailsErrorPredicate`, `isIgnoreCallbackPredicate`, `makeCancelRequest`, `makeFetchCallbackData`, `sendReceipt`, `deleteReceipt`.

Типичная структура пакета `details/<merchant>/`: `Request`, `Response` (implements `MerchantDetailsResponse`), `Callback` (implements `MerchantCallback`), `Method`, `Status` (implements `MerchantOrderStatus`/`MerchantMethod`), абстрактный `<Merchant>OrderCreationService` и конкретные подклассы под разные аккаунты/варианты одного провайдера (`Impl`, `Sim`, `HighCheck`, `LowCheck`, `WT`, `Pdf`, `QR` и т. д.). Подклассы отличаются только `getMerchant()`, `@Qualifier` WebClient'а и классом properties.

Добавление нового мерчанта или варианта затрагивает несколько мест:
1. значение в enum `Merchant` (библиотека `commons`);
2. `properties/<Name>Properties` — record с `@ConfigurationProperties(prefix = "...")`, обычно реализующий общий интерфейс провайдера (сканируется `@ConfigurationPropertiesScan`);
3. бин `<name>WebClient` в `config/WebClientsConfig` (база — `get30SecondsResponseTimeoutWebClientBuilder()`);
4. сервис в `details/<merchant>/`;
5. ключи в `config_template/config.yml` и фиктивные значения в `src/test/resources/application.yml`;
6. тесты: `properties/<Name>PropertiesTest` (`@SpringBootTest`) и `details/<merchant>/*Test` (Mockito).

URL callback'а мерчанту формируется как `callbackConfig.getGatewayUrl() + "/merchant-details/callback?merchant=" + getMerchant().name() + "&secret=" + callbackConfig.getCallbackSecret()`.

### Данные

Сущности: `MerchantConfig` (вкл/выкл, порядок, лимиты сумм, методы, статусы успеха `MerchantSuccessStatus`, автоподтверждение `AutoConfirmConfig`, `groupChatId`, `minDealsCount`, `requiredReceipt`) и `Variable` (глобальные настройки по `VariableType`). Подписи запросов к мерчантам — в `SignatureService`, работа с чеками — в `ReceiptService`.

## Git

- Каждый новый файл сразу после создания добавляй в индекс через `git add <файл>`, но не коммить.

## Прочее

- Логи и сообщения в коде пишутся на русском; при ошибке в лог пишется `System.currentTimeMillis()`, а наружу бросается `ServiceUnavailableException` с этим же таймстампом для поиска по логам.
- `merchant-details.postman_collection.json` — коллекция запросов к REST API сервиса.
