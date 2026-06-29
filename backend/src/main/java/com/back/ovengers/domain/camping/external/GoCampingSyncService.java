package com.back.ovengers.domain.camping.external;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.ImageSyncStatus;
import com.back.ovengers.domain.camping.external.dto.GoCampingApiImageItem;
import com.back.ovengers.domain.camping.external.dto.GoCampingApiItem;
import com.back.ovengers.domain.camping.external.dto.SyncStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoCampingSyncService {

    private final GoCampingClient goCampingClient;
    private final CampingRepository campingRepository;
    private final GoCampingPersistenceService campingPersistenceService;

    public void syncInitialData() {
        List<GoCampingApiItem> items = goCampingClient.getCampList();
        campingPersistenceService.saveCamps(items);
    }

    public void syncImageData() {
        List<Camping> camps = campingRepository.findByImageSyncStatusInAndHostIdIsNullAndDeletedAtIsNull(
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

    public void syncModifiedData(LocalDate syncDate) {
        syncAdded(syncDate);
        syncUpdated(syncDate);
        syncDeleted(syncDate);

        syncImageData();
    }

    private void syncAdded(LocalDate syncDate) {
        List<GoCampingApiItem> items = goCampingClient.getModifiedList(SyncStatus.A, syncDate);
        campingPersistenceService.saveCamps(items);
    }

    private void syncUpdated(LocalDate syncDate) {
        List<GoCampingApiItem> items = goCampingClient.getModifiedList(SyncStatus.U, syncDate);
        campingPersistenceService.updateCamps(items);
    }

    private void syncDeleted(LocalDate syncDate) {
        List<GoCampingApiItem> items = goCampingClient.getModifiedList(SyncStatus.D, syncDate);
        campingPersistenceService.deleteCamps(items);
    }
}
