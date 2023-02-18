package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.skypro.homework.dto.AdsDTO;
import ru.skypro.homework.dto.FullAds;
import ru.skypro.homework.entity.AdEntity;

@Mapper(uses = {ImageMapper.class}, componentModel = "spring")
public interface AdOtherMapper {

    @Mapping(target = "author", source = "author.id")
    @Mapping(target = "pk", source = "id")
    @Mapping(target = "image", source = "imageEntities")
    AdsDTO toAdsDTO(AdEntity adEntity);

    @Mapping(target = "authorFirstName", source = "author.firstName")
    @Mapping(target = "authorLastName", source = "author.lastName")
    @Mapping(target = "pk", source = "id")
    @Mapping(target = "image", source = "imageEntities")
    FullAds toFullAdsDTO(AdEntity adEntity);
}
