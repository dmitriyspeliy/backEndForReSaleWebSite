package ru.skypro.homework.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.AdsDTO;
import ru.skypro.homework.dto.CommentDTO;
import ru.skypro.homework.dto.CreateAds;
import ru.skypro.homework.dto.FullAds;
import ru.skypro.homework.dto.ImageDTO;
import ru.skypro.homework.dto.ResponseWrapperAds;
import ru.skypro.homework.dto.ResponseWrapperComment;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.dto.UserDTO;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.CommentEntity;
import ru.skypro.homework.entity.ImageEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.exception.ElemNotFound;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.mapper.AdMapperImpl;
import ru.skypro.homework.mapper.AdsOtherMapper;
import ru.skypro.homework.mapper.CommentMapper;
import ru.skypro.homework.mapper.ImageMapper;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.repository.AdsRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.ImageRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.impl.AdsServiceImpl;
import ru.skypro.homework.service.impl.SecurityService;

/**
 * Юнит тесты для сервиса
 */
@ExtendWith(MockitoExtension.class)
class AdsServiceTest {

  @InjectMocks
  private AdsService adsService;
  @Mock
  private UserService userService;

  @Mock
  private SecurityService securityService;

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private AdsRepository adsRepository;
  @Mock
  private ImageRepository imageRepository;
  @Mock
  private UserRepository userRepository;

  @Mock
  private AdMapper adMapper = new AdMapperImpl();
  @Mock
  private CommentMapper commentMapper;

  @Mock
  private ImageMapper imageMapper;

  @Mock
  private UserMapper userMapper;

  @Mock
  private AdsOtherMapper adsOtherMapper;


  private MockMultipartFile file;
  private Authentication authentication;
  private UserDTO userDTO;
  private CommentDTO commentDTO;
  private AdEntity adEntity;
  private CommentEntity comment;
  private UserEntity user;
  private ImageEntity image;

  private List<String> listOfImage;
  private Collection<AdsDTO> dtoCollection;

  private List<AdEntity> adEntities;

  private CreateAds createAds;

  private final Integer ONE = 1;
  private final Integer MINUS_ONE = -1;
  private final Integer ZERO = 0;

  AdsServiceTest() {
    adsService = new AdsServiceImpl(adsRepository, commentRepository, userRepository, adMapper,
        commentMapper, imageRepository, imageMapper, userService, userMapper, adsOtherMapper, securityService);
  }

  @BeforeEach
  void init() {
    LocalDateTime date = LocalDateTime.parse("2007-12-03T10:15:30");
    adEntity = new AdEntity(ONE, null, 100, "TitleTest", "TestDescription", null, null);
    user = new UserEntity(ONE, "firstname", "lastname", "user@mgmail.com", "11111111", "+788994455",
        date, "Moscow", "path/to/image",
        List.of(adEntity), null, Role.USER);
    comment = new CommentEntity(ONE, user, date, adEntity, "TextComments");
    image = new ImageEntity(ONE, "path/to/image", adEntity);
    user.setCommentEntities(List.of(comment));
    adEntity.setCommentEntities(List.of(comment));
    adEntity.setImageEntities(List.of(image));
    authentication = Mockito.mock(Authentication.class);
    file
        = new MockMultipartFile(
        "image",
        "image.jpeg",
        MediaType.MULTIPART_FORM_DATA_VALUE,
        "image.jpeg!".getBytes()
    );
    userDTO = new UserDTO("dmitry@gmail.com"
        , "Dmitry", 1, "Pospelov"
        , "89299129121", "20-02-2023 10:12:13", "Moscow", "Реклама");
    listOfImage = new ArrayList<>();
    commentDTO = new CommentDTO(user.getId(),"20-02-2023 10:12:13",adEntity.getId(),"testText");
    dtoCollection = new ArrayList<>();
    adEntities = new ArrayList<>();
    createAds = new CreateAds("testDescc", 11, "testTitle");
  }

  @AfterEach
  void clearAllTestData() {
    adEntity = null;
    comment = null;
    image = null;
    user = null;
    authentication = null;
    file = null;
    userDTO = null;
    listOfImage = null;
    commentDTO = null;
    dtoCollection = null;
    adEntities = null;
  }

  @Test
  void deleteCommentsPositiveTest() {
    lenient().when(adsRepository.findById(anyInt())).thenReturn(Optional.ofNullable(adEntity));
    lenient().when(commentRepository.findById(anyInt())).thenReturn(Optional.ofNullable(comment));
    Assertions.assertThat(adsRepository.findById(anyInt())).isNotNull();
    lenient().doNothing().when(commentRepository).deleteById(anyInt());
  }

  @Test
  void deleteCommentsNegativeTest() {
    lenient().when(adsRepository.findById(anyInt())).thenReturn(Optional.ofNullable(adEntity));
    lenient().when(commentRepository.findById(anyInt())).thenThrow(ElemNotFound.class);
    assertThrows(ElemNotFound.class, () -> commentRepository.findById(anyInt()));
    assertThatExceptionOfType(ElemNotFound.class).isThrownBy( () -> adsService.deleteComments(1,1,authentication));
  }


  @Test
  void addAdsWithAllValidArg() throws IOException {
    when(userService.getUser(any(Authentication.class))).thenReturn(userDTO);
    when(adMapper.toEntity(any(AdsDTO.class))).thenReturn(adEntity);
    when(adsRepository.save(any(AdEntity.class))).thenReturn(adEntity);
    when(imageRepository.save(any(ImageEntity.class))).thenReturn(image);
    when(userMapper.toEntity(any(UserDTO.class))).thenReturn(user);
    when(imageMapper.toEntity(any(ImageDTO.class))).thenReturn(image);


    AdsDTO actual = adsService.addAds(createAds,
        file, authentication);
    actual.setAuthor(adEntity.getId());
    actual.setPk(adEntity.getId());

    AdsDTO excepted = new AdsDTO();
    excepted.setTitle(createAds.getTitle());
    excepted.setPrice(createAds.getPrice());
    excepted.setPk(adEntity.getId());
    listOfImage.add(Base64.getEncoder().encodeToString(file.getBytes()));
    excepted.setImage(image.getPath());
    excepted.setAuthor(adEntity.getId());

    assertEquals(excepted, actual);
    verify(adsRepository, times(ONE)).save(any());


  }

  @Test
  void addAdsWithNoValidArg() {

    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
        () -> adsService.addAds(null, null, null));

  }

  @Test
  void addAdsCommentsWithAllValidArg() {
    when(adsRepository.findById(anyInt())).thenReturn(Optional.ofNullable(adEntity));
    when(commentMapper.toEntity(any(CommentDTO.class))).thenReturn(comment);
    when(userService.getUser(any(Authentication.class))).thenReturn(userDTO);
    when(userMapper.toEntity(any(UserDTO.class))).thenReturn(user);
    when(commentRepository.save(comment)).thenReturn(comment);
    when(commentMapper.toDTO(comment)).thenReturn(commentDTO);
    CommentDTO actual = adsService.addAdsComments(user.getId(),commentDTO,authentication);

    CommentDTO expected = new CommentDTO(user.getId(),"20-02-2023 10:12:13",adEntity.getId(),"testText");

    assertEquals(expected, actual);

    verify(commentRepository, times(ONE)).save(any());


  }

  @Test
  void addAdsCommentsWithNoValidArg() {

    assertThatExceptionOfType(ElemNotFound.class).isThrownBy(
        () -> adsService.addAdsComments(ZERO, commentDTO, authentication));
    assertThatExceptionOfType(ElemNotFound.class).isThrownBy(
        () -> adsService.addAdsComments(MINUS_ONE, commentDTO, authentication));
    assertThatExceptionOfType(ElemNotFound.class).isThrownBy(
        () -> adsService.addAdsComments(ONE, null, authentication));
  }

  @Test
  void getAdsMe() {

    when(userService.getUser(any(Authentication.class))).thenReturn(userDTO);
    when(adMapper.toDTOList(anyCollection())).thenReturn(dtoCollection);
    when(adsRepository.findAll()).thenReturn(adEntities);


    ResponseWrapperAds actual = adsService.getAdsMe(authentication);
    actual.setCount(userDTO.getId());

    ResponseWrapperAds expected = new ResponseWrapperAds(userDTO.getId(),dtoCollection);

    assertEquals(expected, actual);



  }

  @Test
  void removeAdsPositiveTest() {
    when(adsRepository.findById(anyInt())).thenReturn(Optional.ofNullable(adEntity));
    Assertions.assertThat(adsRepository.findById(anyInt())).isNotNull().contains(adEntity)
        .hasValue(adEntity).containsInstanceOf(AdEntity.class);
    lenient().doNothing().when(adsRepository).deleteById(anyInt());
    assertDoesNotThrow(() -> adsRepository.deleteById(anyInt()));
    verify(adsRepository, times(1)).deleteById(anyInt());
  }

  @Test
  void removeAdsNegativeTest() {
    doThrow(new ElemNotFound()).when(adsRepository).deleteById(anyInt());
    Assertions.assertThatThrownBy(() -> adsRepository.deleteById(anyInt()));
    verify(adsRepository, times(1)).deleteById(anyInt());
  }

  @Test
  void uploadImagePositiveTest() throws IOException {
    AdsService adsService = mock(AdsService.class);
    when(adsRepository.findById(1)).thenReturn(Optional.ofNullable(adEntity));
    Assertions.assertThat(adsRepository.findById(1)).isNotNull().contains(adEntity).hasValue(adEntity)
        .containsInstanceOf(AdEntity.class);
    doNothing().when(adsService).uploadImage(1, file);
    Assertions.assertThatNoException().isThrownBy(() ->adsService.uploadImage(1, file));
    verify(adsService, times(1)).uploadImage(1, file);
  }

  @Test
  void uploadImageNegativeTest() throws IOException {
    AdsService adsService = mock(AdsService.class);
    when(adsRepository.findById(1)).thenReturn(Optional.ofNullable(adEntity));
    Assertions.assertThat(adsRepository.findById(1)).isNotNull().contains(adEntity).hasValue(adEntity)
        .containsInstanceOf(AdEntity.class);
    doThrow(ElemNotFound.class).when(adsService).uploadImage(99, file);
    doThrow(new IOException()).when(adsService).uploadImage(1, null);
    Assertions.assertThatThrownBy(() -> adsService.uploadImage(99, file));
    Assertions.assertThatThrownBy(() -> adsService.uploadImage(1, null));
    verify(adsService, times(1)).uploadImage(99, file);
    verify(adsService, times(1)).uploadImage(1, null);
  }

  @Test
  void getAdsCommentsTest() {
    List<CommentEntity> commentEntityList = new ArrayList<>();
    commentEntityList.add(adCommentEntity(1));
    commentEntityList.add(adCommentEntity(2));

    Collection<CommentDTO> commentDTOList = commentMapper.toDTOList(commentEntityList);
    int count = commentDTOList.size();

    ResponseWrapperComment responseWrapperComment = new ResponseWrapperComment(count,
        commentDTOList);

    when(commentRepository.findAllByAd_Id(1)).thenReturn(commentEntityList);
    assertThat(adsService.getAdsComments(1)).isEqualTo(responseWrapperComment);
    verify(commentRepository, times(1)).findAllByAd_Id(any());
  }

//  @Test
//  void getAdsTest() {
//    ResponseWrapperAds responseWrapperAds = new ResponseWrapperAds();
//    List<AdEntity> adEntities = new ArrayList<>();
//    adEntities.add(getAdEntity(1));
//    adEntities.add(getAdEntity(2));
//    Collection<AdsDTO> adsDTOS = adMapper.toDTOList(adEntities);
//    responseWrapperAds.setResults(adsDTOS);
//    responseWrapperAds.setCount(2);
//    when(adsRepository.findAll()).thenReturn(adEntities);
//    when(adsService.getAds()).thenReturn(responseWrapperAds);
//    assertThat(adsService.getAds()).isEqualTo(responseWrapperAds);
//    verify(adsRepository,times(1)).findAll();
//  }

//  @Test
//  void getCommentsTest() {
//    CommentEntity commentEntity = adCommentEntity(1);
//    CommentDTO commentDTO = new CommentDTO(1, "20-02-2023 14:20:10", 1, "123456789");
//    when(commentRepository.findByIdAndAd_Id(1, 1)).thenReturn(Optional.of(commentEntity));
//    assertThat(adsService.getComments(1, 1)).isEqualTo(commentDTO);
//    verify(commentRepository,times(1)).findByIdAndAd_Id(any(),any());
//  }

  @Test
  void getAdByIdTest() {
    UserEntity author = getAuthor();
    List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    Authentication authentication = new TestingAuthenticationToken(author.getEmail(), author.getPassword(), authorities);
    authentication.setAuthenticated(true);
    FullAds fullAds = adsOtherMapper.toFullAds(getAdEntity(1));
    when(adsRepository.findById(1)).thenReturn(Optional.of(getAdEntity(1)));
    lenient().when(securityService.isAdsUpdateAvailable(any(Authentication.class),anyInt())).thenReturn(true);
    assertThat(adsService.getAdById(1,authentication)).isEqualTo(fullAds);
    verify(adsRepository,times(1)).findById(any());
  }

  @Test
  void getCommentsTestNegative() {
    assertThatExceptionOfType(ElemNotFound.class).isThrownBy(() -> adsService.getComments(1,1));
  }
  @Test
  void getAdByIdTestNegative() {
    UserEntity author = getAuthor();
    List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
    Authentication authentication = new TestingAuthenticationToken(author.getEmail(), author.getPassword(), authorities);

    assertThatExceptionOfType(ElemNotFound.class).isThrownBy(() -> adsService.getAdById(1,authentication));
  }

  private AdEntity getAdEntity(int id) {
    AdEntity adEntity = new AdEntity();
    List<ImageEntity> imageEntities = new ArrayList<>();
    ImageEntity imageEntity = new ImageEntity(id, "/path/to/image/1", new AdEntity());

    imageEntities.add(imageEntity);

    adEntity.setId(id);
    adEntity.setTitle("afsdf");
    adEntity.setPrice(123);
    adEntity.setDescription("asfsdf");
    adEntity.setAuthor(getAuthor());
    adEntity.setImageEntities(imageEntities);
    return adEntity;
  }

  private CommentEntity adCommentEntity(int id) {
    CommentEntity commentEntity = new CommentEntity();
    commentEntity.setId(id);
    commentEntity.setAuthor(getAuthor());
    commentEntity.setCreatedAt(LocalDateTime.of(2023, 02, 20, 14, 20, 10));
    commentEntity.setAd(getAdEntity(1));
    commentEntity.setText("123456789");
    return commentEntity;
  }

  private UserEntity getAuthor() {
    UserEntity author = new UserEntity();
    author.setImage("/users/author.1");
    author.setLastName("Иванов");
    author.setFirstName("Иван");
    author.setPassword("1111");
    author.setCity("MSK");
    author.setPhone("+79876543210");
    author.setEmail("mail@mail.ru");
    author.setRegDate(LocalDateTime.of(2023, 02, 20, 14, 20, 10));
    author.setId(1);

    return author;
  }


}