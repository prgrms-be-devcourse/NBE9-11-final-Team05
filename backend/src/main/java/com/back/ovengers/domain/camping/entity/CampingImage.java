package com.back.ovengers.domain.camping.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "camping_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
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

    private String objectKey;

    // 외부 API에서 수집한 이미지를 생성
    public static CampingImage from(Camping camping, String imageUrl) {
        return CampingImage.builder()
                .imageUrl(imageUrl)
                .camping(camping)
                .build();
    }

    //호스트가 직접 업로드한 이미지를 생성
    // imageUrl(CloudFront URL)과 objectKey(S3 경로)를 함께 저장
    public static CampingImage create(
            Camping camping,
            String imageUrl,
            String objectKey
    ) {
        return CampingImage.builder()
                .camping(camping)
                .imageUrl(imageUrl)
                .objectKey(objectKey)
                .build();
    }

}
