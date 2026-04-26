package tgb.cryptoexchange.merchantdetails.dto;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Data;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class MerchantConfigRequest {

    private Merchant merchant;

    private Integer merchantOrder;

    private String sort;

    public <T> List<Predicate> toPredicates(Root<T> root, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        if (Objects.nonNull(merchantOrder)) {
            predicates.add(cb.equal(root.get("merchantOrder"), merchantOrder));
        }
        if (Objects.nonNull(merchant)) {
            predicates.add(cb.equal(root.get("merchant"), merchant));
        }
        return predicates;
    }
}
