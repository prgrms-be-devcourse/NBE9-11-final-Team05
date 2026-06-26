package com.back.ovengers.domain.camping.service;

import com.back.ovengers.domain.camping.dto.CampingImageCreateResponse;
import com.back.ovengers.domain.camping.dto.HostCampingImageResponse;
import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingImage;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingImageRepository;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.s3.S3Service;
import com.back.ovengers.global.s3.S3UploadResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CampingImageServiceTest {

    @Mock
    private CampingRepository campingRepository;

    @Mock
    private CampingImageRepository campingImageRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private CampingImageService campingImageService;

    @Test
    void 이미지_등록_성공_firstImageUrl이_없으면_대표이미지로_설정된다() {
        // given
        Long userId = 1L;
        Long campingId = 10L;

        User host = createHost(userId);
        Camping camping = createCamping(campingId, host, null);

        MultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image".getBytes()
        );

        S3UploadResult uploadResult = new S3UploadResult(
                "campings/10/test.jpg",
                "https://cdn.test.com/campings/10/test.jpg"
        );

        given(campingRepository.findByIdAndDeletedAtIsNull(campingId))
                .willReturn(Optional.of(camping));

        given(s3Service.upload(file, campingId))
                .willReturn(uploadResult);

        given(campingImageRepository.save(any(CampingImage.class)))
                .willAnswer(invocation -> {
                    CampingImage image = invocation.getArgument(0);
                    ReflectionTestUtils.setField(image, "id", 100L);
                    return image;
                });

        // when
        HostCampingImageResponse response =
                campingImageService.addCampingImage(userId, campingId, file, false);

        // then
        assertThat(camping.getFirstImageUrl()).isEqualTo(uploadResult.imageUrl());
        assertThat(response.imageUrl()).isEqualTo(uploadResult.imageUrl());
        assertThat(response.thumbnail()).isTrue();

        verify(s3Service).upload(file, campingId);
        verify(campingImageRepository).save(any(CampingImage.class));
    }

    @Test
    void 이미지_등록_성공_thumbnail_true면_기존_대표이미지를_새_이미지로_교체한다() {
        // given
        Long userId = 1L;
        Long campingId = 10L;

        User host = createHost(userId);
        Camping camping = createCamping(campingId, host, "https://cdn.test.com/old.jpg");

        MultipartFile file = new MockMultipartFile(
                "image",
                "new.jpg",
                "image/jpeg",
                "new image".getBytes()
        );

        S3UploadResult uploadResult = new S3UploadResult(
                "campings/10/new.jpg",
                "https://cdn.test.com/campings/10/new.jpg"
        );

        given(campingRepository.findByIdAndDeletedAtIsNull(campingId))
                .willReturn(Optional.of(camping));

        given(s3Service.upload(file, campingId))
                .willReturn(uploadResult);

        given(campingImageRepository.save(any(CampingImage.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        HostCampingImageResponse response =
                campingImageService.addCampingImage(userId, campingId, file, true);

        // then
        assertThat(camping.getFirstImageUrl()).isEqualTo(uploadResult.imageUrl());
        assertThat(response.thumbnail()).isTrue();
    }

    @Test
    void 이미지_등록_성공_thumbnail_false이고_기존_대표이미지가_있으면_유지된다() {
        // given
        Long userId = 1L;
        Long campingId = 10L;
        String oldThumbnail = "https://cdn.test.com/old.jpg";

        User host = createHost(userId);
        Camping camping = createCamping(campingId, host, oldThumbnail);

        MultipartFile file = new MockMultipartFile(
                "image",
                "new.jpg",
                "image/jpeg",
                "new image".getBytes()
        );

        S3UploadResult uploadResult = new S3UploadResult(
                "campings/10/new.jpg",
                "https://cdn.test.com/campings/10/new.jpg"
        );

        given(campingRepository.findByIdAndDeletedAtIsNull(campingId))
                .willReturn(Optional.of(camping));

        given(s3Service.upload(file, campingId))
                .willReturn(uploadResult);

        given(campingImageRepository.save(any(CampingImage.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        HostCampingImageResponse response =
                campingImageService.addCampingImage(userId, campingId, file, false);

        // then
        assertThat(camping.getFirstImageUrl()).isEqualTo(oldThumbnail);
        assertThat(response.thumbnail()).isFalse();
    }

    @Test
    void 이미지_삭제_성공_objectKey가_있으면_S3에서도_삭제한다() {
        // given
        Long userId = 1L;
        Long campingId = 10L;
        Long imageId = 100L;

        User host = createHost(userId);
        Camping camping = createCamping(campingId, host, "https://cdn.test.com/thumbnail.jpg");

        CampingImage image = createCampingImage(
                imageId,
                camping,
                "https://cdn.test.com/normal.jpg",
                "campings/10/normal.jpg"
        );

        given(campingRepository.findByIdAndDeletedAtIsNull(campingId))
                .willReturn(Optional.of(camping));

        given(campingImageRepository.findByIdAndCampingId(imageId, campingId))
                .willReturn(Optional.of(image));

        // when
        campingImageService.deleteCampingImage(userId, campingId, imageId);

        // then
        verify(s3Service).delete("campings/10/normal.jpg");
        verify(campingImageRepository).delete(image);
    }

    @Test
    void 대표이미지_삭제_성공_남은_이미지가_있으면_대표이미지를_교체한다() {
        // given
        Long userId = 1L;
        Long campingId = 10L;
        Long imageId = 100L;

        User host = createHost(userId);
        Camping camping = createCamping(campingId, host, "https://cdn.test.com/thumbnail.jpg");

        CampingImage thumbnailImage = createCampingImage(
                imageId,
                camping,
                "https://cdn.test.com/thumbnail.jpg",
                "campings/10/thumbnail.jpg"
        );

        CampingImage nextImage = createCampingImage(
                101L,
                camping,
                "https://cdn.test.com/next.jpg",
                "campings/10/next.jpg"
        );

        given(campingRepository.findByIdAndDeletedAtIsNull(campingId))
                .willReturn(Optional.of(camping));

        given(campingImageRepository.findByIdAndCampingId(imageId, campingId))
                .willReturn(Optional.of(thumbnailImage));

        given(campingImageRepository.findFirstByCampingIdAndIdNotOrderByIdAsc(campingId, imageId))
                .willReturn(Optional.of(nextImage));

        // when
        campingImageService.deleteCampingImage(userId, campingId, imageId);

        // then
        assertThat(camping.getFirstImageUrl()).isEqualTo("https://cdn.test.com/next.jpg");
        verify(s3Service).delete("campings/10/thumbnail.jpg");
        verify(campingImageRepository).delete(thumbnailImage);
    }

    @Test
    void 대표이미지_삭제_성공_남은_이미지가_없으면_대표이미지를_null로_변경한다() {
        // given
        Long userId = 1L;
        Long campingId = 10L;
        Long imageId = 100L;

        User host = createHost(userId);
        Camping camping = createCamping(campingId, host, "https://cdn.test.com/thumbnail.jpg");

        CampingImage thumbnailImage = createCampingImage(
                imageId,
                camping,
                "https://cdn.test.com/thumbnail.jpg",
                "campings/10/thumbnail.jpg"
        );

        given(campingRepository.findByIdAndDeletedAtIsNull(campingId))
                .willReturn(Optional.of(camping));

        given(campingImageRepository.findByIdAndCampingId(imageId, campingId))
                .willReturn(Optional.of(thumbnailImage));

        given(campingImageRepository.findFirstByCampingIdAndIdNotOrderByIdAsc(campingId, imageId))
                .willReturn(Optional.empty());

        // when
        campingImageService.deleteCampingImage(userId, campingId, imageId);

        // then
        assertThat(camping.getFirstImageUrl()).isNull();
        verify(s3Service).delete("campings/10/thumbnail.jpg");
        verify(campingImageRepository).delete(thumbnailImage);
    }

    @Test
    void 외부_API_이미지_삭제시_objectKey가_null이면_S3_삭제를_호출하지_않는다() {
        // given
        Long userId = 1L;
        Long campingId = 10L;
        Long imageId = 100L;

        User host = createHost(userId);
        Camping camping = createCamping(campingId, host, "https://external.com/image.jpg");

        CampingImage externalImage = createCampingImage(
                imageId,
                camping,
                "https://external.com/image.jpg",
                null
        );

        given(campingRepository.findByIdAndDeletedAtIsNull(campingId))
                .willReturn(Optional.of(camping));

        given(campingImageRepository.findByIdAndCampingId(imageId, campingId))
                .willReturn(Optional.of(externalImage));

        given(campingImageRepository.findFirstByCampingIdAndIdNotOrderByIdAsc(campingId, imageId))
                .willReturn(Optional.empty());

        // when
        campingImageService.deleteCampingImage(userId, campingId, imageId);

        // then
        verify(s3Service, never()).delete(anyString());
        verify(campingImageRepository).delete(externalImage);
        assertThat(camping.getFirstImageUrl()).isNull();
    }

    @Test
    void 다른_호스트의_캠핑장에_이미지_등록하면_예외가_발생한다() {
        // given
        Long ownerId = 1L;
        Long otherUserId = 2L;
        Long campingId = 10L;

        User host = createHost(ownerId);
        Camping camping = createCamping(campingId, host, null);

        MultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image".getBytes()
        );

        given(campingRepository.findByIdAndDeletedAtIsNull(campingId))
                .willReturn(Optional.of(camping));

        // when & then
        assertThatThrownBy(() ->
                campingImageService.addCampingImage(otherUserId, campingId, file, false)
        ).isInstanceOf(CustomException.class);

        verify(s3Service, never()).upload(any(), anyLong());
        verify(campingImageRepository, never()).save(any());
    }

    private User createHost(Long id) {
        User user = User.builder()
                .email("host@test.com")
                .password("password")
                .name("호스트")
                .nickname("host")
                .phone("010-0000-0000")
                .role(Role.HOST)
                .status(Status.ACTIVE)
                .build();

        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Camping createCamping(Long id, User host, String firstImageUrl) {
        Camping camping = Camping.builder()
                .host(host)
                .name("테스트 캠핑장")
                .region("경기")
                .city("가평")
                .address("테스트 주소")
                .firstImageUrl(firstImageUrl)
                .status(CampingStatus.APPROVED)
                .build();

        ReflectionTestUtils.setField(camping, "id", id);
        return camping;
    }

    private CampingImage createCampingImage(
            Long id,
            Camping camping,
            String imageUrl,
            String objectKey
    ) {
        CampingImage image = CampingImage.builder()
                .camping(camping)
                .imageUrl(imageUrl)
                .objectKey(objectKey)
                .build();

        ReflectionTestUtils.setField(image, "id", id);
        return image;
    }
}
