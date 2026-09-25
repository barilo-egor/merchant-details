package tgb.cryptoexchange.merchantdetails.details.payscrow;

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
    SBP("2ec6dbd6-49a5-45d0-bd6d-b0134ee4639a", "СБП", RequestMethod.SBP),
    BANK_CARD("8fe3669a-a448-4053-bc4b-43bb51cb3e9d", "Карта", RequestMethod.CARD),
    TRANS_SBP("c154be74-5e4e-4edb-85c2-d2aa175165f5", "Трансгран СБП", null),
    BEELINE("f671777f-ab51-49af-b322-b0a1647568d2", "Билайн сим карты", null),
    TRIANGLE("53035abd-b34e-4bd7-9305-e5546320a3b9", "Треугольники", null),
    ALFA("df920cd2-321a-4da0-b1b5-832e32c4da0b", "Альфа-альфа", null),
    PSB("70c73aa1-163b-4721-856a-6e1ed8c5e4cb", "ПСБ-ПСБ", null),
    GAZ_PROM("9469a889-f5cc-4d86-970f-45cfe30a8f13","Газпром-газпром", null),
    OZON("e556810d-86fd-40fd-bbb4-d67cc59cbcc5", "Озон-озон", null),
    SIM("94eb8ffc-f900-47e1-89dd-ed8974780595", "SIM", null)
    ;

    private final String value;

    private final String description;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }

    public static class Serializer extends JsonSerializer<Method> {

        @Override
        public void serialize(Method payscrowMethod, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(payscrowMethod.getValue());
        }
    }
}
