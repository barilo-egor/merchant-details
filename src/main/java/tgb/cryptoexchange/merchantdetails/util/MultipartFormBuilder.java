package tgb.cryptoexchange.merchantdetails.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Сборщик тела {@code multipart/form-data} для {@link org.springframework.web.client.RestClient}.
 * <p>
 * Повторяет используемую часть API {@code org.springframework.http.client.MultipartBodyBuilder},
 * который при загрузке требует reactive-streams.
 */
public class MultipartFormBuilder {

    private final List<PartBuilder> parts = new ArrayList<>();

    /**
     * Добавляет часть формы.
     *
     * @param name  имя части
     * @param value значение: строка, {@link org.springframework.core.io.Resource} или другой объект,
     *              для которого есть конвертер
     * @return билдер части для указания имени файла и типа содержимого
     */
    public PartBuilder part(String name, Object value) {
        PartBuilder partBuilder = new PartBuilder(name, value);
        parts.add(partBuilder);
        return partBuilder;
    }

    /**
     * Добавляет часть формы с типом содержимого.
     *
     * @param name        имя части
     * @param value       значение части
     * @param contentType тип содержимого части
     * @return билдер части
     */
    public PartBuilder part(String name, Object value, MediaType contentType) {
        return part(name, value).contentType(contentType);
    }

    /**
     * Собирает тело формы.
     *
     * @return части формы в порядке добавления
     */
    public MultiValueMap<String, HttpEntity<?>> build() {
        MultiValueMap<String, HttpEntity<?>> result = new LinkedMultiValueMap<>();
        for (PartBuilder part : parts) {
            result.add(part.name, new HttpEntity<>(part.value, part.headers));
        }
        return result;
    }

    /**
     * Билдер отдельной части формы.
     */
    public static final class PartBuilder {

        private final String name;

        private final Object value;

        private final HttpHeaders headers = new HttpHeaders();

        private PartBuilder(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        /**
         * Задаёт имя файла в заголовке {@code Content-Disposition} части.
         *
         * @param filename имя файла
         * @return этот билдер
         */
        public PartBuilder filename(String filename) {
            headers.setContentDispositionFormData(name, filename);
            return this;
        }

        /**
         * Задаёт тип содержимого части.
         *
         * @param contentType тип содержимого
         * @return этот билдер
         */
        public PartBuilder contentType(MediaType contentType) {
            headers.setContentType(contentType);
            return this;
        }
    }
}
