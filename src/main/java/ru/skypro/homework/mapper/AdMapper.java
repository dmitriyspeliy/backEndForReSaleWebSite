package ru.skypro.homework.mapper;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.skypro.homework.dto.AdsDTO;
import ru.skypro.homework.dto.CreateAds;
import ru.skypro.homework.dto.FullAds;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.ImageEntity;

/**
 * маппер для {@link AdEntity} готовый рекорд {@link AdsDTO}
 */
@Mapper(componentModel = "spring", uses = {ImageMapper.class})
public interface AdMapper {

  @Mapping(target = "description", source = "description")
  @Mapping(target = "price", source = "price")
  @Mapping(target = "title", source = "title")
  CreateAds toCreateAds(AdEntity adEntity);


  @Mapping(target = "author.id", source = "author")
  @Mapping(target = "id", source = "pk")
  @Mapping(target = "description", constant = "Неполная реклама")
  @Mapping(target = "imageEntities", expression = "java(setImageEntities(adDto.getImage()))")
  AdEntity toEntity(AdsDTO adDto);



  @Mapping(target = "author", source = "author.id")
  @Mapping(target = "pk", source = "id")
  @Mapping(target = "image", expression = "java(setImage(adEntity.getImageEntities()))")
  AdsDTO toDTO(AdEntity adEntity);

  default List<String> setImage(List<ImageEntity> imageEntities) {
    if (imageEntities == null || imageEntities.size() == 0) {
      return null;
    }
    return imageEntities
        .stream()
        .map(ImageEntity::getPath)
        .collect(Collectors.toList());
  }

  default List<ImageEntity> setImageEntities(List<String> image) {
    if (image == null || image.size() == 0) {
      return null;
    }
    List<ImageEntity> imageEntities = new ArrayList<>();
    for (String s : image) {
      ImageEntity imageEntity = new ImageEntity();
      imageEntity.setPath(s);
      imageEntities.add(imageEntity);
    }
    return imageEntities;
  }

  @Mapping(target = "pk", source = "adEntity.id")
  @Mapping(target = "authorFirstName", source = "author.firstName")
  @Mapping(target = "authorLastName", source = "author.lastName")
  @Mapping(target = "description", source = "adEntity.description")
  @Mapping(target = "email", source = "author.email")
//  @Mapping(target = "image", expression = "java(imageEntities.stream().map(ImageEntity::getPath).collect(Collectors.toList()))")
//  @Mapping(target = "image", expression = "java(adEntity.getImageEntities().stream().map(imageEntity -> imageEntity.getPath()).collect(Collectors.toUnmodifiableList()))")
  @Mapping(target = "image", source = "imageEntities")
  @Mapping(target = "phone", source = "author.phone")
  @Mapping(target = "price", source = "adEntity.price")
  @Mapping(target = "title", source = "adEntity.title")
  FullAds toFullAds(AdEntity adEntity);

//  default FullAds toFullAds(AdEntity adEntity, UserEntity userEntity, ImageEntity imageEntity) {
//    if (adEntity == null) {
//      return null;
//    }
//    FullAds fullAds = new FullAds();
//    fullAds.setAuthorFirstName(userEntity.getFirstName());
//    fullAds.setAuthorLastName(userEntity.getLastName());
//    fullAds.setDescription(adEntity.getDescription());
//    fullAds.setPk(userEntity.getId());
//    fullAds.setEmail(userEntity.getEmail());
//    fullAds.setPrice(adEntity.getPrice());
//    fullAds.setPhone(userEntity.getPhone());
//    fullAds.setImage(List.of(imageEntity.getPath()));
//    fullAds.setTitle(adEntity.getTitle());
//    return fullAds;
//  }

  Collection<AdEntity> toEntityList(Collection<AdsDTO> adDTOS);

  Collection<AdsDTO> toDTOList(Collection<AdEntity> adEntities);
}
