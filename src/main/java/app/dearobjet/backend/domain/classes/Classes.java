package app.dearobjet.backend.domain.classes;

import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "classes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class Classes extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "classes_id")
    private Long classesId;

    @Column(name = "class_name")
    private String className;

    @Column(name = "class_description")
    private String classDescription;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "class_images", joinColumns = @JoinColumn(name = "classes_id"))
    @OrderColumn(name = "image_order")
    @Column(name = "image_url", nullable = false)
    private List<String> classImageUrls = new ArrayList<>();

    private Double price;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    public void updateClassInfo(
            String className,
            String classDescription,
            Double price,
            Integer maxCapacity,
            String notes
    ) {
        this.className = className;
        this.classDescription = classDescription;
        this.price = price;
        this.maxCapacity = maxCapacity;
        this.notes = notes;
    }

    public void updateClassImageUrls(List<String> classImageUrls) {
        this.classImageUrls.clear();
        if (classImageUrls != null) {
            this.classImageUrls.addAll(classImageUrls);
        }
    }
}
