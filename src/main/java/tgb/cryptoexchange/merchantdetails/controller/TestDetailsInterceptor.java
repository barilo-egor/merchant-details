package tgb.cryptoexchange.merchantdetails.controller;

import io.grpc.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class TestDetailsInterceptor implements ServerInterceptor {

    public static final Metadata.Key<String> TEST_DETAILS_HEADER_KEY =
            Metadata.Key.of("x-test-details", Metadata.ASCII_STRING_MARSHALLER);

    public static final Context.Key<String> TEST_DETAILS_CTX_KEY = Context.key("testDetails");

    @Override
    public <T1, T2> ServerCall.Listener<T1> interceptCall(
            ServerCall<T1, T2> call,
            Metadata headers,
            ServerCallHandler<T1, T2> next) {

        String mockValue = headers.get(TEST_DETAILS_HEADER_KEY);
        Context context = Context.current().withValue(TEST_DETAILS_CTX_KEY, mockValue);
        return Contexts.interceptCall(context, call, headers, next);
    }

}
