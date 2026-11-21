package net.rcetech.md.domain.repository;

import jakarta.persistence.criteria.Predicate;
import lombok.experimental.UtilityClass;
import net.rcetech.md.domain.entity.MerchantConfig;
import net.rcetech.md.domain.entity.MerchantConfig_;
import net.rcetech.md.meta.dto.MerchantConfigFilter;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Спецификации выборки конфигов мерчантов.
 */
@UtilityClass
public class MerchantConfigSpecifications {

    /**
     * Условие соответствия конфига фильтру.
     *
     * @param filter фильтр, может быть {@code null}
     * @return спецификация, без фильтра пропускающая все конфиги
     */
    public static PredicateSpecification<MerchantConfig> matches(MerchantConfigFilter filter) {
        return (from, builder) -> {
            if (filter == null) {
                return builder.conjunction();
            }
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(filter.merchantOrder())) {
                predicates.add(builder.equal(from.get(MerchantConfig_.merchantOrder), filter.merchantOrder()));
            }
            if (!CollectionUtils.isEmpty(filter.merchants())) {
                predicates.add(from.get(MerchantConfig_.merchant).in(filter.merchants()));
            }
            return builder.and(predicates);
        };
    }
}
