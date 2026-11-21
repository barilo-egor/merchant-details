package net.rcetech.md.integrations.serialize;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateSerializer {

    private DateSerializer() {
    }

    public static class ISO8601 extends ValueSerializer<LocalDateTime> {

        @Override
        public void serialize(LocalDateTime dateTime, JsonGenerator jsonGenerator, SerializationContext serializerProvider) {
            jsonGenerator.writeString(dateTime.format(DateTimeFormatter.ISO_DATE_TIME));
        }
    }
}
