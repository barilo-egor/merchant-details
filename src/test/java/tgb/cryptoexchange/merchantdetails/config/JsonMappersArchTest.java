package tgb.cryptoexchange.merchantdetails.config;

import com.tngtech.archunit.core.domain.JavaCall;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import tools.jackson.databind.ObjectMapper;

import static com.tngtech.archunit.core.domain.JavaCall.Predicates.target;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.nameMatching;
import static com.tngtech.archunit.core.domain.properties.HasOwner.Predicates.With.owner;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "tgb.cryptoexchange.merchantdetails")
class JsonMappersArchTest {

    private static final String REASON = "маппер с дефолтами Jackson 3 меняет формат JSON, используйте бин ObjectMapper или JsonMappers";

    @ArchTest
    static final ArchRule mappersShouldNotBeConstructedDirectly = noClasses()
            .that().doNotBelongToAnyOf(JsonMappers.class)
            .should().callConstructorWhere(target(owner(assignableTo(ObjectMapper.class))))
            .because(REASON);

    @ArchTest
    static final ArchRule mappersShouldNotBeBuiltDirectly = noClasses()
            .that().doNotBelongToAnyOf(JsonMappers.class)
            .should().callMethodWhere(JavaCall.Predicates.<JavaCall<?>>target(owner(assignableTo(ObjectMapper.class)))
                    .and(target(nameMatching("builder|builderWithJackson2Defaults|shared"))))
            .because(REASON);
}
