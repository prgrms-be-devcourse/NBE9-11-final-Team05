package com.back.ovengers.domain.camping.external;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingImage;
import com.back.ovengers.domain.camping.external.dto.GoCampingApiImageItem;
import com.back.ovengers.domain.camping.external.dto.GoCampingApiItem;
import com.back.ovengers.domain.camping.repository.CampingImageRepository;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoCampingPersistenceService {

    private final CampingRepository campingRepository;
    private final CampingImageRepository campingImageRepository;
    private final ChatService chatService;

    @Transactional
    public void saveCamps(List<GoCampingApiItem> items) {
        List<Long> apiContentIds = items.stream()
                .map(item -> Long.valueOf(item.contentId()))
                .toList();

        Set<Long> existingIds = new HashSet<>(campingRepository.findContentIdsIn(apiContentIds));

        List<GoCampingApiItem> newItems = items.stream()
                .filter(item -> !existingIds.contains(Long.valueOf(item.contentId())))
                .toList();

        List<Camping> camps = new ArrayList<>();

        for (GoCampingApiItem item : newItems) {
            camps.add(Camping.from(item));
        }

        List<Camping> savedCamps = campingRepository.saveAll(camps);

        for (Camping camp : savedCamps) {
            chatService.createOpenChatRoom(
                    camp.getId(),
                    camp.getName()
            );
        }
    }

    @Transactional
    public void saveCampImages(Camping camp, List<GoCampingApiImageItem> items) {
        List<CampingImage> images = new ArrayList<>();

        for (GoCampingApiImageItem item : items) {
            images.add(
                    CampingImage.from(camp, item.imageUrl())
            );
        }

        if (!images.isEmpty()) {
            campingImageRepository.saveAll(images);
        }
    }

    @Transactional
    public void updateCamps(List<GoCampingApiItem> items) {
        Map<Long, Camping> campingMap = getCampingMap(items);

        for(GoCampingApiItem item : items) {
            Camping camping = campingMap.get(Long.valueOf(item.contentId()));

            if (camping == null) {
                continue;
            }

            camping.updateFromApi(item);
            campingImageRepository.deleteAllByCamping(camping);
        }
    }

    @Transactional
    public void deleteCamps(List<GoCampingApiItem> items) {
        Map<Long, Camping> campingMap = getCampingMap(items);

        for(GoCampingApiItem item : items) {
            Camping camping = campingMap.get(Long.valueOf(item.contentId()));

            if (camping == null) {
                continue;
            }

            camping.delete();
            campingImageRepository.deleteAllByCamping(camping);
        }
    }

    private Map<Long, Camping> getCampingMap(List<GoCampingApiItem> items) {
        List<Long> contentIds = items.stream()
                .map(item -> Long.valueOf(item.contentId()))
                .toList();

        List<Camping> camps = campingRepository.findByContentIdIn(contentIds);

        return camps.stream()
                .collect(Collectors.toMap(
                        Camping::getContentId,
                        camp -> camp
                ));
    }
}
