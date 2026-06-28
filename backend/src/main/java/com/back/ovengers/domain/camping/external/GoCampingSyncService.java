package com.back.ovengers.domain.camping.external;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.ImageSyncStatus;
import com.back.ovengers.domain.camping.external.dto.GoCampingApiImageItem;
import com.back.ovengers.domain.camping.external.dto.GoCampingApiItem;
import com.back.ovengers.domain.camping.repository.CampingImageRepository;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoCampingSyncService {

    private final GoCampingClient goCampingClient;
    private final CampingRepository campingRepository;
    private final CampingImageRepository campingImageRepository;
    private final GoCampingPersistenceService campingPersistenceService;

    public void syncInitialData() {
        List<GoCampingApiItem> items = goCampingClient.getCampList();

        List<Long> apiContentIds = items.stream()
                        .map(item -> Long.valueOf(item.contentId()))
                        .toList();

        Set<Long> existingIds = new HashSet<>(campingRepository.findContentIdsIn(apiContentIds));

        List<GoCampingApiItem> newItems = items.stream()
                        .filter(item -> !existingIds.contains(Long.valueOf(item.contentId())))
                        .toList();

        campingPersistenceService.saveCamps(newItems);
    }

    public void syncImageData() {
        List<Camping> camps = campingRepository.findByImageSyncStatusInAndHostIdIsNull(
                List.of(ImageSyncStatus.PENDING, ImageSyncStatus.FAILED)
        );

        int successCount = 0;
        int failCount = 0;

        for (Camping camp : camps) {
            Long contentId = camp.getContentId();

            try {
                List<GoCampingApiImageItem> items = goCampingClient.getCampImageList(contentId);
                campingPersistenceService.saveCampImages(camp, items);

                camp.updateImageSyncStatus(ImageSyncStatus.DONE);

                successCount++;

            } catch (HttpClientErrorException.TooManyRequests e) {
                log.warn(
                        "하루 API 호출 횟수 초과로 이미지 동기화를 중단합니다. campId={}, contentId={}",
                        camp.getId(),
                        contentId
                );
                break;

            } catch (Exception e) {
                camp.updateImageSyncStatus(ImageSyncStatus.FAILED);

                failCount++;

                log.error(
                        "Failed to sync images. campId={}, contentId={}",
                        camp.getId(),
                        contentId,
                        e
                );
            }
        }

        log.info(
                "이미지 동기화 완료. 성공={}, 실패={}",
                successCount,
                failCount
        );
    }

    // 임시 채팅방 데이터 생성 (기존 호출된 캠핑장 API)
    public void createMissingOpenChatRooms() {
        campingPersistenceService.createMissingOpenChatRooms();
    }
}
