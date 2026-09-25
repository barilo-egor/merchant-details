package tgb.cryptoexchange.merchantdetails.details.gambit;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    CARD("a0e3edeb-725a-4aec-ab9d-756e201659bb", "Карта", RequestMethod.CARD),
    SBP("a0e3edeb-773d-446d-8a06-48cb102ea182", "СБП", RequestMethod.SBP),
    MOBILE("a0e3edeb-80f0-4d5d-914e-064a94cde4e3", "Sim", null);

    final String methodUid;

    final String description;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }

    public static class Serializer extends JsonSerializer<Method> {
        @Override
        public void serialize(Method method, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(method.getMethodUid());
        }
    }
}
