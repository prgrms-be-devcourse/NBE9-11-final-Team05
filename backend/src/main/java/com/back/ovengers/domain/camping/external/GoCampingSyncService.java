package com.back.ovengers.domain.camping.external;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingImage;
import com.back.ovengers.domain.camping.external.dto.GoCampingApiImageItem;
import com.back.ovengers.domain.camping.external.dto.GoCampingApiItem;
import com.back.ovengers.domain.camping.repository.CampingImageRepository;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoCampingSyncService {

    private final GoCampingClient goCampingClient;
    private final CampingRepository campingRepository;
    private final CampingImageRepository campingImageRepository;

    @Transactional
    public void syncInitialData() {
        if(campingRepository.count() > 0) {
            throw new CustomException(ErrorCode.INITIAL_DATA_ALREADY_EXISTS);
        }

        List<GoCampingApiItem> items = goCampingClient.getCampList();
        List<Camping> camps = new ArrayList<>();

        for (GoCampingApiItem item : items) {
            camps.add(Camping.from(item));
        }

        campingRepository.saveAll(camps);
    }

    @Transactional
    public void syncImageData() {
        List<Camping> camps = campingRepository.findAll();

        for(Camping camp : camps) {
            Long contentId = camp.getContentId();
            if(contentId == null) continue;

            campingImageRepository.deleteByCampingId(camp.getId());

            List<GoCampingApiImageItem> items = goCampingClient.getCampImageList(contentId);
            List<CampingImage> savedImages = new ArrayList<>();

            for(GoCampingApiImageItem item : items) {
                savedImages.add(CampingImage.from(camp, item.imageUrl()));
            }

            if (!savedImages.isEmpty()) {
                campingImageRepository.saveAll(savedImages);
            }
        }
    }

}
