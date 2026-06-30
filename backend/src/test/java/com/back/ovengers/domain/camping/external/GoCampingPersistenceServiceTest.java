package com.back.ovengers.domain.camping.external;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.external.dto.GoCampingApiImageItem;
import com.back.ovengers.domain.camping.external.dto.GoCampingApiItem;
import com.back.ovengers.domain.camping.repository.CampingImageRepository;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.chat.service.ChatService;
import com.back.ovengers.fixture.CampingFixture;
import com.back.ovengers.fixture.GoCampingApiItemFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoCampingPersistenceServiceTest {
    @Mock
    private CampingRepository campingRepository;

    @Mock
    private CampingImageRepository campingImageRepository;

    @Mock
    private ChatService chatService;

    @InjectMocks
    private GoCampingPersistenceService service;

    @Test
    @DisplayName("신규 캠핑만 저장되고 채팅방이 생성된다")
    void saveCamps_createsChatRoomsForNewCampsOnly() {
        // given
        GoCampingApiItem item1 = GoCampingApiItemFixture.create("1", "camp1");
        GoCampingApiItem item2 = GoCampingApiItemFixture.create("2", "camp2");

        when(campingRepository.findContentIdsIn(List.of(1L, 2L)))
                .thenReturn(List.of());

        Camping camping1 = CampingFixture.builder()
                .name("camp1")
                .build();
        ReflectionTestUtils.setField(camping1, "id", 1L);

        Camping camping2 = CampingFixture.builder()
                .name("camp2")
                .build();
        ReflectionTestUtils.setField(camping2, "id", 2L);

        when(campingRepository.saveAll(anyList()))
                .thenReturn(List.of(camping1, camping2));

        // when
        service.saveCamps(List.of(item1, item2));

        // then
        verify(campingRepository).saveAll(anyList());

        verify(chatService).createOpenChatRoom(1L, "camp1");
        verify(chatService).createOpenChatRoom(2L, "camp2");
    }

    @Test
    @DisplayName("이미지가 존재하면 저장한다")
    void saveCampImages_savesImages() {
        // given
        Camping camping = CampingFixture.builder().build();

        GoCampingApiImageItem image1 = new GoCampingApiImageItem("url1");
        GoCampingApiImageItem image2 = new GoCampingApiImageItem("url2");

        // when
        service.saveCampImages(camping, List.of(image1, image2));

        // then
        verify(campingImageRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("이미지가 없으면 저장하지 않는다")
    void saveCampImages_doesNotSaveWhenEmpty() {
        // given
        Camping camping = CampingFixture.builder().build();

        // when
        service.saveCampImages(camping, List.of());

        // then
        verify(campingImageRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("캠핑 정보를 수정하고 이미지를 삭제한다")
    void updateCamps_updatesCampingAndDeletesImages() {
        // given
        GoCampingApiItem item1 = GoCampingApiItemFixture.create("1", "camp1");

        Camping camping = CampingFixture.builder()
                .contentId(1L)
                .name("캠핑장1")
                .build();

        when(campingRepository.findByContentIdIn(List.of(1L)))
                .thenReturn(List.of(camping));

        // when
        service.updateCamps(List.of(item1));

        // then
        assertThat(camping.getName()).isEqualTo("camp1");
        verify(campingImageRepository).deleteAllByCamping(camping);
    }

    @Test
    @DisplayName("캠핑을 삭제하고 이미지를 삭제한다")
    void deleteCamps_deletesCampingAndImages() {
        // given
        GoCampingApiItem item1 = GoCampingApiItemFixture.create("1", "camp1");

        Camping camping = CampingFixture.builder()
                .contentId(1L)
                .build();

        when(campingRepository.findByContentIdIn(List.of(1L)))
                .thenReturn(List.of(camping));

        // when
        service.deleteCamps(List.of(item1));

        // then
        assertNotNull(camping.getDeletedAt());
        verify(campingImageRepository).deleteAllByCamping(camping);
    }


}
