package com.back.ovengers.domain.camping.service;

import com.back.ovengers.domain.camping.dto.CampingDetailResponse;
import com.back.ovengers.domain.camping.dto.CampingListResponse;
import com.back.ovengers.domain.site.dto.SiteResponse;
import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingImage;
import com.back.ovengers.domain.camping.repository.CampingImageRepository;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampingService {

    private final CampingRepository campingRepository;
    private final CampingImageRepository campingImageRepository;
    private final SiteRepository siteRepository;

    public Page<CampingListResponse> getCampList(String keyword, Pageable pageable) {

        String keywordLike = !StringUtils.hasText(keyword) ? null : "%" + keyword + "%";
        Page<Camping> pagedCamps = campingRepository.searchCamping(keywordLike, pageable);

        return pagedCamps.map(CampingListResponse::from);
    }

    public CampingDetailResponse getCampDetail(Long campingId) {
        Camping camp = campingRepository.findById(campingId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAMPING_NOT_FOUND));

        List<String> imageUrls = campingImageRepository.findByCampingId(campingId)
                .stream()
                .map(CampingImage::getImageUrl)
                .toList();

        List<SiteResponse> sites = siteRepository.findByCampingId(campingId)
                .stream()
                .map(SiteResponse::from)
                .toList();

        return CampingDetailResponse.from(camp, imageUrls, sites);
    }
}
