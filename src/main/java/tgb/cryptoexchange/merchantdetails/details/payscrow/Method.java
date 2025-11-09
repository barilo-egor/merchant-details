package tgb.cryptoexchange.merchantdetails.details.payscrow;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

@AllArgsConstructor
@Getter
public enum Method {
    SBP("2ec6dbd6-49a5-45d0-bd6d-b0134ee4639a", "СБП"),
    BANK_CARD("8fe3669a-a448-4053-bc4b-43bb51cb3e9d", "Карта"),
    TRANS_SBP("c154be74-5e4e-4edb-85c2-d2aa175165f5", "Трансгран СБП"),
    BEELINE("f671777f-ab51-49af-b322-b0a1647568d2", "Билайн сим карты"),
    TRIANGLE("53035abd-b34e-4bd7-9305-e5546320a3b9", "Треугольники")
    ;

    private final String value;

    private final String description;

    public static class Serializer extends JsonSerializer<Method> {

        @Override
        public void serialize(Method payscrowMethod, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(payscrowMethod.getValue());
        }
    }
}
