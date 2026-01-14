package app.dearobjet.backend.domain.contract.entity;

import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.shop.entity.Product;
import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "contract_products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ContractProduct extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_products_id")
    private Long contractProductsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_artist_contracts_id", nullable = false)
    private ShopArtistContract shopArtistContract;

    @Column(name = "listing_status")
    private String listingStatus;

    @Column(name = "listed_at")
    private java.time.LocalDateTime listedAt;

    @Column(name = "ended_at")
    private java.time.LocalDateTime endedAt;

    @Column(name = "last_modified_at")
    private java.time.LocalDateTime lastModifiedAt;

    @Column(name = "sold_quantity")
    private Integer soldQuantity;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "products_id")
    private Product products;

    @Column(name = "selling_price")
    private Double sellingPrice;
}