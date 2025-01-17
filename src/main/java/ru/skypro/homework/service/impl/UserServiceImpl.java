package ru.skypro.homework.service.impl;


import static java.nio.file.StandardOpenOption.CREATE_NEW;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.UserDTO;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.exception.ElemNotFound;
import ru.skypro.homework.loger.FormLogInfo;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.UserService;

/**
 * Сервис пользователей
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Value("${image.user.dir.path}")
  private String userPhotoDir;

  public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
  }

  /**
   * Получить данные пользователя
   */
  @Override
  public UserDTO getUser(Authentication authentication) {
    log.info(FormLogInfo.getInfo());
    String nameEmail = authentication.getName();
    UserEntity userEntity = findEntityByEmail(nameEmail);
    return userMapper.toDTO(userEntity);
  }

  /**
   * Обновить данные пользователя
   */
  @Override
  public UserDTO updateUser(UserDTO newUserDto, Authentication authentication) {
    log.info(FormLogInfo.getInfo());

    String nameEmail = authentication.getName();
    UserEntity userEntity = findEntityByEmail(nameEmail);
    int id = userEntity.getId();

    UserEntity oldUser = findById(id);

    oldUser.setEmail(userEntity.getEmail());
    oldUser.setAdEntities(userEntity.getAdEntities());
    oldUser.setFirstName(newUserDto.getFirstName());
    oldUser.setLastName(newUserDto.getLastName());
    oldUser.setPhone(newUserDto.getPhone());

    try {
      oldUser.setRegDate(userEntity.getRegDate());
    } catch (Exception e) {
      log.info("Ошибка изменения даты регистрации");
    }

    oldUser.setCity(newUserDto.getCity());
    oldUser.setImage(userEntity.getImage());
    userRepository.save(oldUser);

    return userMapper.toDTO(oldUser);
  }

  /**
   * Установить пароль пользователя
   */
  @Override
  public NewPassword setPassword(NewPassword newPassword) {
    log.info(FormLogInfo.getInfo());
    return null;
  }

  /**
   * загрузить аватарку пользователя
   */
  @Override
  public void updateUserImage(MultipartFile image, Authentication authentication) {
    log.info(FormLogInfo.getInfo());

    String nameEmail = authentication.getName();
    UserEntity userEntity = findEntityByEmail(nameEmail);
    String linkToGetImage = "/users" + "/" + userEntity.getId();
    Path filePath = Path.of(userPhotoDir,
        Objects.requireNonNull(String.valueOf(userEntity.getId())));

    if(userEntity.getImage() != null){
      try {
        Files.deleteIfExists(filePath);
        userEntity.setImage(null);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

    }


    try {
      Files.createDirectories(filePath.getParent());
      Files.deleteIfExists(filePath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try (InputStream is = image.getInputStream();
        OutputStream os = Files.newOutputStream(filePath, CREATE_NEW);
        BufferedInputStream bis = new BufferedInputStream(is, 1024);
        BufferedOutputStream bos = new BufferedOutputStream(os, 1024)) {

      bis.transferTo(bos);

      userEntity.setImage(linkToGetImage);
      userRepository.save(userEntity);

    } catch (Exception e) {
      log.info("Ошибка сохранения файла");
    }

  }

  /**
   * получить фото пользователя
   *
   * @return фото конвертированная в массив байтов
   */
  public byte[] getPhotoById(Integer id) {
    String linkToGetImage = "user_photo_dir/" + id;
    byte[] bytes;
    try {
      bytes = Files.readAllBytes(Paths.get(linkToGetImage));
    } catch (IOException e) {
      log.info(FormLogInfo.getCatch());
      throw new RuntimeException(e);
    }
    return bytes;
  }

  /**
   * найти пользователя по id
   *
   * @param id id пользователя
   * @return пользователь
   */
  private UserEntity findById(int id) {
    log.info(FormLogInfo.getInfo());
    return userRepository.findById(id).orElseThrow(ElemNotFound::new);
  }

  /**
   * найти пользователя по email - логину
   *
   * @param email email - логину пользователя
   * @return пользователь
   */
  private UserEntity findEntityByEmail(String email) {
    log.info(FormLogInfo.getInfo());
    return userRepository.findByEmail(email).get();
  }


}
