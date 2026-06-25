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
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoCampingPersistenceService {

    private final CampingRepository campingRepository;
    private final CampingImageRepository campingImageRepository;
    private final ChatService chatService;

    @Transactional
    public void saveCamps(List<GoCampingApiItem> items) {
        List<Camping> camps = new ArrayList<>();

        for (GoCampingApiItem item : items) {
            camps.add(Camping.from(item));
        }

        campingRepository.saveAll(camps);

        for (Camping camp : camps) {
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
}
