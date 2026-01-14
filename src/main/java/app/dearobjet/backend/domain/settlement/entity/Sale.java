package app.dearobjet.backend.domain.settlement.entity;

import app.dearobjet.backend.domain.contract.entity.ContractProduct;
import app.dearobjet.backend.domain.contract.entity.ShopArtistContract;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sales")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sales_id")
    private Long salesId;

    @Column(name = "sale_at")
    private java.time.LocalDateTime saleAt;

    @Column(name = "saled_amount")
    private Double saledAmount;

    @Column(name = "saled_price")
    private Double saledPrice;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @Column(name = "last_modified_at")
    private java.time.LocalDateTime lastModifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_products_id")
    private ContractProduct contractProduct;
}