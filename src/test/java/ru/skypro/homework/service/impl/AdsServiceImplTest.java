package ru.skypro.homework.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.skypro.homework.dto.AdsDTO;
import ru.skypro.homework.dto.CommentDTO;
import ru.skypro.homework.dto.CreateAds;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.CommentEntity;
import ru.skypro.homework.entity.ImageEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.exception.ElemNotFound;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.mapper.AdMapperImpl;
import ru.skypro.homework.mapper.CommentMapper;
import ru.skypro.homework.mapper.CommentMapperImpl;
import ru.skypro.homework.repository.AdsRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdsServiceImplTest {

    @Mock
    AdsRepository adsRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    SecurityService securityService;
    @Mock
    UserRepository userRepository;
    @Mock
    AdMapper adMapper;
    @Mock
    CommentMapper commentMapper;
    @InjectMocks
    AdsServiceImpl adsService;
    AdMapper adImplMapper = new AdMapperImpl();
    CommentMapper commentImplMapper = new CommentMapperImpl();
    @Test
    void updateComments() {
        int sourceCommentId = 2;
        int sourceAdsId = 1;
        CommentDTO sourceCommentDTO = getCommentDTOA();
        CommentEntity commentEntity = getCommentEntityA();
        Authentication authentication = getTestAuthentication();

        lenient().when(securityService.isCommentUpdateAvailable(
                any(Authentication.class),anyInt(),anyInt())).thenReturn(true);
        lenient().when(commentRepository.findByIdAndAd_Id(sourceCommentId, sourceAdsId))
                .thenReturn(Optional.of(getCommentEntityA()));
        lenient().when(userRepository.findById(3)).thenReturn(Optional.of(getNewCommentAuthorA()));

        commentEntity.setAuthor(getNewCommentAuthorA());
        commentEntity.setText("Реклама");
        commentEntity.setCreatedAt(LocalDateTime.of(2023, 02, 20, 10, 12,13));

        lenient().when(commentRepository.save(commentEntity)).thenReturn(commentEntity);
        lenient().when(commentMapper.toDTO(commentEntity)).thenReturn(commentImplMapper.toDTO(commentEntity));

        CommentDTO excepted = adsService.updateComments(sourceAdsId, sourceCommentId, sourceCommentDTO, authentication);
        CommentDTO actual = getCommentDTOA();

        assertEquals(excepted,actual);
    }

    @Test
    void updateCommentsNegativeNotFoundComment() {
        Authentication authentication = getTestAuthentication();

        lenient().when(commentRepository.findByIdAndAd_Id(anyInt(), anyInt()))
                .thenReturn(Optional.of(getCommentEntityA()));
        lenient().when(userRepository.findById(anyInt())).thenThrow(ElemNotFound.class);
        lenient().when(securityService.isCommentUpdateAvailable(any(Authentication.class),
                anyInt(), anyInt())).thenReturn(true);

        assertThrows(ElemNotFound.class, () -> adsService.updateComments(1,1, getCommentDTOA(), authentication));
    }

    @Test
    void updateCommentsNegativeNotFoundUser() {
        UserEntity author = getAuthorA();
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        Authentication authentication = new TestingAuthenticationToken(author.getEmail(), author.getPassword(), authorities);
        authentication.setAuthenticated(true);
        lenient().when(commentRepository.findByIdAndAd_Id(anyInt(),anyInt())).thenThrow(ElemNotFound.class);
        lenient().when(securityService.isAdmin(any(Authentication.class))).thenReturn(true);
        assertThrows(ElemNotFound.class, () -> adsService.updateComments(1,1, getCommentDTOA(), authentication));
    }

    @Test
    void updateAds() {
        CreateAds sourceCreateAds = getCreateAdsA();
        UserEntity author = getAuthorA();
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        Authentication authentication = new TestingAuthenticationToken(author.getEmail(), author.getPassword(), authorities);
        authentication.setAuthenticated(true);
        int sourceId = 1;
        when(adsRepository.findById(anyInt())).thenReturn(Optional.of(getAdEntityA()));
        when(adsRepository.save(any(AdEntity.class))).thenReturn(getResultAdEntityA());
        when(adMapper.toDTO(getResultAdEntityA())).thenReturn(adImplMapper.toDTO(
            getResultAdEntityA()));
        when(securityService.isAdsUpdateAvailable(any(Authentication.class),anyInt())).thenReturn(true);
        AdsDTO excepted = adsService.updateAds(sourceId, sourceCreateAds, authentication);
        AdsDTO actual = getResultAdsDTOA();

        assertEquals(excepted,actual);
    }

    @Test
    void updateAdsNegativeTest() {
        UserEntity author = getAuthorA();
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        Authentication authentication = new TestingAuthenticationToken(author.getEmail(), author.getPassword(), authorities);
        lenient().when(adsRepository.findById(anyInt())).thenReturn(Optional.empty());
        assertThrows(ElemNotFound.class, () -> adsService.updateAds(1, getCreateAdsA(),authentication));
    }

    private CreateAds getCreateAdsA() {
        return new CreateAds("Описание", 99, "Заголовок");
    }

    private AdEntity getAdEntityA() {
        AdEntity adEntity = new AdEntity();
        adEntity.setId(1);

        List<ImageEntity> imageEntities = new ArrayList<>();
        imageEntities.add(new ImageEntity(1, "/ads/image/1", adEntity));

        adEntity.setImageEntities(imageEntities);
        adEntity.setTitle("Title");
        adEntity.setDescription("Description");
        adEntity.setCommentEntities(Collections.emptyList());
        adEntity.setPrice(100);
        adEntity.setAuthor(getAuthorA());

        return adEntity;
    }

    private AdEntity getResultAdEntityA() {
        AdEntity adEntity = new AdEntity();
        adEntity.setId(1);

        List<ImageEntity> imageEntities = new ArrayList<>();
        imageEntities.add(new ImageEntity(1, "/ads/image/1", adEntity));

        adEntity.setImageEntities(imageEntities);
        adEntity.setTitle("Заголовок");
        adEntity.setDescription("Описание");
        adEntity.setCommentEntities(Collections.emptyList());
        adEntity.setPrice(99);
        adEntity.setAuthor(getAuthorA());

        return adEntity;
    }

    private AdsDTO getResultAdsDTOA() {
        AdsDTO adsDTO = new AdsDTO();
        adsDTO.setPk(1);
        adsDTO.setTitle("Заголовок");
        adsDTO.setPrice(99);

        List<String> images = new ArrayList<>();
        images.add("/ads/image/1");

        adsDTO.setImage("/ads/image/1");
        adsDTO.setAuthor(2);
        return adsDTO;
    }

    private UserEntity getAuthorA() {
        UserEntity author = new UserEntity();
        author.setImage("/users/author.png");
        author.setLastName("Иванов");
        author.setFirstName("Иван");
        author.setCity("MSK");
        author.setPhone("+79876543210");
        author.setEmail("mail@mail.ru");
        author.setRegDate(LocalDateTime.of(2023, 02, 20, 14, 20, 10));
        author.setId(2);

        return author;
    }

    private UserEntity getNewCommentAuthorA() {
        UserEntity author = new UserEntity();
        author.setImage("/users/authorComment.png");
        author.setLastName("Иванов");
        author.setFirstName("Иван");
        author.setCity("MSK");
        author.setPhone("+79876543210");
        author.setEmail("mail@mail.ru");
        author.setRegDate(LocalDateTime.of(2023, 02, 20, 14, 20, 10));
        author.setId(3);

        return author;
    }

    private CommentEntity getCommentEntityA() {
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setText("Text");
        commentEntity.setCreatedAt(LocalDateTime.of(2023, 02, 22, 14, 20, 10));
        commentEntity.setAuthor(getAuthorA());
        commentEntity.setId(2);
        commentEntity.setAd(getAdEntityA());

        return commentEntity;
    }

    private CommentDTO getCommentDTOA() {
        return new CommentDTO(3,"20-02-2023 10:12:13",2, "Реклама");
    }

    private Authentication getTestAuthentication() {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        UserEntity author = getAuthorA();
        return new TestingAuthenticationToken(author.getEmail(), author.getPassword(), authorities);
    }
}