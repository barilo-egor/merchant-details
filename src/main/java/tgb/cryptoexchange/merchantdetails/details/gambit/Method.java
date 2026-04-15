package tgb.cryptoexchange.merchantdetails.details.gambit;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;

import java.io.IOException;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    CARD("a0e3edeb-725a-4aec-ab9d-756e201659bb", "Карта"),
    SBP("a0e3edeb-773d-446d-8a06-48cb102ea182", "СБП");

    final String methodUid;

    final String description;

    public static class Serializer extends JsonSerializer<Method> {
        @Override
        public void serialize(Method method, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(method.getMethodUid());
        }
    }
}
