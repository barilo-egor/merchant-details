package tgb.cryptoexchange.merchantdetails.dto;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.entity.MerchantConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class MerchantConfigRequest {

    private Integer merchantOrder;

    private Boolean isOn;

    private String sort;

    public List<Predicate> toPredicates(Root<MerchantConfig> root, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        if (Objects.nonNull(merchantOrder)) {
            predicates.add(cb.equal(root.get("merchantOrder"), merchantOrder));
        }
        if (isOn != null) {
            predicates.add(cb.equal(root.get("isOn"), isOn));
        }

        return predicates;
    }
}
