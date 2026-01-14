package app.dearobjet.backend.domain.order.entity;

import app.dearobjet.backend.domain.classes.Classes;
import app.dearobjet.backend.domain.shop.entity.Shop;
import app.dearobjet.backend.domain.user.entity.User;
import app.dearobjet.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_class")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderClass extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_class_id")
    private Long orderClassId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_id")
    private Order order;

    @Column(name = "last_modified_at")
    private java.time.LocalDateTime lastModifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classes_id")
    private Classes classes;
}