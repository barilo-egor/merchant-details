package tgb.cryptoexchange.merchantdetails.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class MerchantSuccessStatus {

    @Id
    @GeneratedValue
    private Long id;

    private String status;
}
