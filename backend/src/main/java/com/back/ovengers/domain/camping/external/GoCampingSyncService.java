package com.back.ovengers.domain.camping.external;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.external.dto.GoCampingApiImageItem;
import com.back.ovengers.domain.camping.external.dto.GoCampingApiItem;
import com.back.ovengers.domain.camping.repository.CampingImageRepository;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
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
        if (campingRepository.count() > 0) {
            throw new CustomException(ErrorCode.INITIAL_DATA_ALREADY_EXISTS);
        }

        List<GoCampingApiItem> items = goCampingClient.getCampList();
        campingPersistenceService.saveCamps(items);
    }

    public void syncImageData() {
        List<Camping> camps = campingRepository.findAll();
        Set<Long> campingIdsWithImages =
                campingImageRepository.findCampingIdsWithImages();

        List<Long> failedCampIds = new ArrayList<>();
        int successCount = 0;

        for (Camping camp : camps) {
            if (campingIdsWithImages.contains(camp.getId())) {
                continue;
            }

            Long contentId = camp.getContentId();
            if (contentId == null) {
                continue;
            }

            try {
                List<GoCampingApiImageItem> items = goCampingClient.getCampImageList(contentId);
                campingPersistenceService.saveCampImages(camp, items);

                successCount++;

            } catch (HttpClientErrorException.TooManyRequests e) {
                log.warn(
                        "하루 API 호출 횟수 초과로 이미지 동기화를 중단합니다. campId={}, contentId={}",
                        camp.getId(),
                        contentId
                );
                break;

            } catch (Exception e) {
                failedCampIds.add(camp.getId());

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
                failedCampIds.size()
        );
        log.info("이미지 동기화 실패 항목 : {}", failedCampIds);
    }

}
