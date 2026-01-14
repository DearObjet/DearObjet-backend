package app.dearobjet.backend.domain.shop.entity;

import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "products_id")
    private Long productsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id")
    private Artist artist;

    @Column(name = "product_name")
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double price;

    private String status;

    @Column(name = "available_quantity")
    private Integer availableQuantity;

    @Column(name = "product_url")
    private String productUrl;
}
