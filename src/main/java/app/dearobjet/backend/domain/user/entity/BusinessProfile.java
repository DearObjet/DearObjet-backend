package app.dearobjet.backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "business_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BusinessProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_type")
    private String businessType;  // SHOP, ARTIST

    @Column(name = "business_number")
    private String businessNumber;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "owner_name")
    private String ownerName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}