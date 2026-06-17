package com.back.ovengers.domain.camping.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Entity
@Table(name = "camping_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CampingImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camping_id", nullable = false)
    private Camping camping;

    @NotNull
    @Column(nullable = false)
    private String imageUrl;

    public static CampingImage from(Camping camping, String imageUrl) {
        return CampingImage.builder()
                .imageUrl(imageUrl)
                .camping(camping)
                .build();
    }

}
