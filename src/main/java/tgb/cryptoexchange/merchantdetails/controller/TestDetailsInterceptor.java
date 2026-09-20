package tgb.cryptoexchange.merchantdetails.controller;

import io.grpc.*;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class TestDetailsInterceptor implements ServerInterceptor {

    public static final Metadata.Key<String> TEST_DETAILS_HEADER_KEY =
            Metadata.Key.of("x-test-details", Metadata.ASCII_STRING_MARSHALLER);

    public static final Context.Key<String> TEST_DETAILS_CTX_KEY = Context.key("testDetails");

    private final boolean isDevOrTest;

    public TestDetailsInterceptor(Environment env) {
        this.isDevOrTest = Arrays.stream(env.getActiveProfiles())
                .anyMatch(p -> p.equalsIgnoreCase("dev"));
    }

    @Override
    public <T1, T2> ServerCall.Listener<T1> interceptCall(
            ServerCall<T1, T2> call,
            Metadata headers,
            ServerCallHandler<T1, T2> next) {
        if (!isDevOrTest) {
            return next.startCall(call, headers);
        }

        String mockValue = headers.get(TEST_DETAILS_HEADER_KEY);
        Context context = Context.current().withValue(TEST_DETAILS_CTX_KEY, mockValue);
        return Contexts.interceptCall(context, call, headers, next);
    }

}
