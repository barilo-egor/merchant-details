package tgb.cryptoexchange.merchantdetails.details.settlex;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

@AllArgsConstructor
@Getter
public enum Method {
    SBP("cmhhrccre0shany01q8oah3cd", "СБП"),
    C2C("cmhhrcwo40szlny01q71c4djw", "Карта");

    private final String value;

    private final String displayName;

    public static class Serializer extends JsonSerializer<Method> {
        @Override
        public void serialize(Method method, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(method.getValue());
        }
    }
}
