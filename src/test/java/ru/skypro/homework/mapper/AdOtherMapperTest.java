package ru.skypro.homework.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.skypro.homework.dto.AdsDTO;
import ru.skypro.homework.dto.FullAds;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.ImageEntity;
import ru.skypro.homework.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class AdOtherMapperTest {

    @Autowired
    private AdOtherMapper adOtherMapper;

    @Test
    void toAdsDTO() {
        AdEntity adEntity = new AdEntity();
        adEntity.setId(1);
        adEntity.setPrice(999);
        adEntity.setTitle("Title");
        adEntity.setDescription("Description");
        adEntity.setCommentEntities(List.of());
        adEntity.setAuthor(getAuthor());
        adEntity.setImageEntities(getImageEntities());

        AdsDTO exceptedDto = new AdsDTO();
        exceptedDto.setPrice(999);
        exceptedDto.setPk(1);
        exceptedDto.setTitle("Title");
        exceptedDto.setAuthor(2);
        exceptedDto.setImage(getImagePathString());

        assertEquals(exceptedDto, adOtherMapper.toAdsDTO(adEntity));
    }

    @Test
    void toFullAds() {
        AdEntity adEntity = new AdEntity();
        adEntity.setId(1);
        adEntity.setPrice(999);
        adEntity.setTitle("Title");
        adEntity.setDescription("Description");
        adEntity.setCommentEntities(List.of());
        adEntity.setAuthor(getAuthor());
        adEntity.setImageEntities(getImageEntities());

        FullAds exceptedDto = new FullAds();
        exceptedDto.setPrice(999);
        exceptedDto.setPk(1);
        exceptedDto.setTitle("Title");
        exceptedDto.setDescription("Description");
        exceptedDto.setAuthorFirstName("Иван");
        exceptedDto.setAuthorLastName("Иванов");
        exceptedDto.setPhone("7884643");
        exceptedDto.setEmail("asda@asd.re");

        exceptedDto.setImage(getImagePathString());

        assertEquals(exceptedDto, adOtherMapper.toFullAdsDTO(adEntity));
    }

    private UserEntity getAuthor() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(2);
        userEntity.setRegDate(LocalDateTime.of(2023, 02, 16, 14,30, 22));
        userEntity.setPhone("7884643");
        userEntity.setEmail("asda@asd.re");
        userEntity.setCity("MSK");
        userEntity.setFirstName("Иван");
        userEntity.setLastName("Иванов");
        userEntity.setImage("/user/image/1");
        return userEntity;
    }

    private List<ImageEntity> getImageEntities() {
        List<ImageEntity> imageEntities = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            ImageEntity imageEntity = new ImageEntity();
            imageEntity.setId(i);;
            imageEntity.setPath("/ads/images/" + i);
            imageEntities.add(imageEntity);
        }

        return imageEntities;
    }

    private List<String> getImagePathString() {
        List<String> imagePathStrings = new ArrayList<>();
        imagePathStrings.add("/ads/images/" + 1);
        imagePathStrings.add("/ads/images/" + 2);
        imagePathStrings.add("/ads/images/" + 3);
        imagePathStrings.add("/ads/images/" + 4);
        return imagePathStrings;
    }
}