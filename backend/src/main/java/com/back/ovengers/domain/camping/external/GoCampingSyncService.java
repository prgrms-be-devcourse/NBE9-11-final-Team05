package com.back.ovengers.domain.camping.external;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class GoCampingSyncService {

    private final GoCampingClient goCampingClient;
    private final CampingRepository campingRepository;

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

    public void syncUpdatedData() {

    }

    public void syncImages() {

    }

}
