package com.back.ovengers.domain.camping.service;

import com.back.ovengers.domain.camping.dto.CampingListResponse;
import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CampingService {

    private final CampingRepository campingRepository;

    public Page<CampingListResponse> getCamps(String keyword, Pageable pageable) {

        String keywordLike = (keyword == null) ? null : "%" + keyword + "%";
        Page<Camping> pagedCamps = campingRepository.searchCamping(keywordLike, pageable);

        return pagedCamps.map(CampingListResponse::from);
    }
}
