package com.back.ovengers.domain.camping.service;

import com.back.ovengers.domain.camping.dto.CampingImageCreateResponse;
import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingImage;
import com.back.ovengers.domain.camping.repository.CampingImageRepository;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import com.back.ovengers.global.s3.S3Service;
import com.back.ovengers.global.s3.S3UploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampingImageService {

    private final CampingRepository campingRepository;
    private final CampingImageRepository campingImageRepository;
    private final S3Service s3Service;

    /**
     * 캠핑장 이미지 등록
     */
    @Transactional
    public CampingImageCreateResponse addCampingImage(
            Long userId,
            Long campingId,
            MultipartFile image,
            boolean thumbnail
    ) {
        // 1. 캠핑장 존재 여부 확인
        Camping camping = getCamping(campingId);

        // 2. 캠핑장 소유 호스트 검증
        validateOwner(camping, userId);

        // 3. S3 업로드 후 objectKey, imageUrl 반환
        S3UploadResult uploadResult = s3Service.upload(image, campingId);

        CampingImage campingImage = CampingImage.create(
                camping,
                uploadResult.imageUrl(),
                uploadResult.objectKey()
        );

        // 4. 이미지 정보 DB 저장
        campingImageRepository.save(campingImage);

        // 5. 대표 이미지(thumbnail) 설정
        if (thumbnail || camping.getFirstImageUrl() == null) {
            camping.changeFirstImageUrl(uploadResult.imageUrl());
        }

        return CampingImageCreateResponse.from(
                campingImage,
                camping.getFirstImageUrl()
        );
    }

    /**
     * 캠핑장 이미지 삭제
     */
    @Transactional
    public void deleteCampingImage(
            Long userId,
            Long campingId,
            Long imageId
    ) {
        // 1. 캠핑장 존재 여부 확인
        Camping camping = getCamping(campingId);

        // 2. 캠핑장 소유 호스트 검증
        validateOwner(camping, userId);

        CampingImage image = campingImageRepository
                .findByIdAndCampingId(imageId, campingId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAMPING_IMAGE_NOT_FOUND));

        // 삭제 대상이 현재 대표 이미지인지 확인
        boolean wasThumbnail = image.getImageUrl().equals(camping.getFirstImageUrl());

        // 3. S3 파일 삭제 (호스트 업로드 이미지인 경우)
        // 외부 API 이미지의 경우 objectKey는 null
        if (image.getObjectKey() != null) {
            s3Service.delete(image.getObjectKey());
        }

        // 4. DB 이미지 삭제 (hard delete)
        campingImageRepository.delete(image);

        // 5. 삭제된 이미지가 대표 이미지였다면 대표 이미지 재설정
        // 남아있는 이미지 중 가장 먼저 등록된 이미지를 대표 이미지로 변경
        if (wasThumbnail) {
            campingImageRepository
                    .findFirstByCampingIdAndIdNotOrderByIdAsc(campingId, imageId)
                    .ifPresentOrElse(
                            nextImage -> camping.changeFirstImageUrl(nextImage.getImageUrl()),
                            () -> camping.changeFirstImageUrl(null)
                    );
        }
    }

    private Camping getCamping(Long campingId) {
        return campingRepository.findByIdAndDeletedAtIsNull(campingId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAMPING_NOT_FOUND));
    }

    private void validateOwner(Camping camping, Long userId) {
        if (!camping.getHost().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_CAMPING_OWNER);
        }
    }
}