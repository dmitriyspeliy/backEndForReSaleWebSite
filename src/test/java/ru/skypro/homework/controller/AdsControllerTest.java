package ru.skypro.homework.controller;

import java.util.ArrayList;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.skypro.homework.WebSecurityConfigTest;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.CommentEntity;
import ru.skypro.homework.entity.ImageEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.mapper.AdsOtherMapper;
import ru.skypro.homework.mapper.CommentMapper;
import ru.skypro.homework.mapper.ImageMapper;
import ru.skypro.homework.repository.AdsRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.security.UserDetailServiceImpl;
import ru.skypro.homework.service.AdsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdsController.class)
@Import(value = WebSecurityConfigTest.class)
class AdsControllerTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private MockMvc mockMvc;
    @InjectMocks
    private AdsController adsController;
    @MockBean
    private AdsService adsService;
    @MockBean
    private UserDetailServiceImpl userDetailsService;
    @MockBean
    private AdsRepository adsRepository;
    @MockBean
    private AdsOtherMapper adsOtherMapper;
    @MockBean
    private ImageMapper imageMapper;
    @MockBean
    private CommentRepository commentRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private CommentMapper commentMapper;



    @Test
    public void contextLoads() {
        assertNotNull(adsController);
    }

    @Test
    @WithMockUser(value = "user@gmail.com")
    void createAds() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        Authentication auth = Mockito.mock(Authentication.class);
        MockMultipartFile image = new MockMultipartFile("image", "image.jpeg",
            MediaType.IMAGE_JPEG_VALUE, "image.jpeg".getBytes());
        String url = "/ads";
        CreateAds createAds = getCreateAds();
        JSONObject createAdsJSON =  new JSONObject();
        createAdsJSON.put("description", createAds.getDescription());
        createAdsJSON.put("price", createAds.getPrice());
        createAdsJSON.put("title", createAds.getPrice());
        MockMultipartFile json = new MockMultipartFile("properties", "properties",
                MediaType.APPLICATION_JSON_VALUE, createAdsJSON.toString().getBytes());
        when(adsService.addAds(getCreateAds(), image, auth)).thenReturn(getAdsDTO());
        mockMvc.perform(multipart(url, HttpMethod.POST)
                .file(image)
                        .file(json))
            .andDo(print())
            .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(value = "user@gmail.com")
    void addAdsComments() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        Authentication auth = Mockito.mock(Authentication.class);
        CommentDTO commentDTO = getCommentDTO();
        JSONObject commentDTOJSON =  new JSONObject();
        commentDTOJSON.put("author", commentDTO.getAuthor());
        commentDTOJSON.put("createdAt", commentDTO.getCreatedAt());
        commentDTOJSON.put("pk", commentDTO.getPk());
        commentDTOJSON.put("text", commentDTO.getText());
        when(adsService.addAdsComments(1,commentDTO, auth)).thenReturn(commentDTO);
        when(adsRepository.findById(anyInt())).thenReturn(Optional.of(getAdEntity()));
        String url = "/ads/{ad_pk}/comments";
        mockMvc.perform(post(url, 1)
            .content(String.valueOf(commentDTOJSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(value = "user@gmail.com")
    void deleteComments() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        Authentication auth = Mockito.mock(Authentication.class);
        String url = "/ads/{adPk}/comments/{id}";
        AdEntity adEntity = getAdEntity();
        CommentEntity commentEntity = getCommentEntity();
        when(adsRepository.findById(1)).thenReturn(Optional.of(adEntity));
        when(commentRepository.findById(1)).thenReturn(Optional.of(commentEntity));
        doNothing().when(adsService).deleteComments(1,1, auth);
        mockMvc.perform(delete(url, 1,1))
            .andDo(print())
            .andExpect(status().isOk());

    }
    @Test
    @WithMockUser(value = "user@gmail.com")
    void removeAds() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        Authentication auth = Mockito.mock(Authentication.class);
        String url = "/ads/{id}";
        AdEntity adEntity = getAdEntity();
        when(adsRepository.findById(1)).thenReturn(Optional.of(adEntity));
        doNothing().when(adsService).removeAds(1, auth);
        mockMvc.perform(delete(url, 1))
            .andDo(print())
            .andExpect(status().isOk());

    }


    @Test
    @WithMockUser(value = "user@gmail.com")
    void updateComments() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        Authentication auth = Mockito.mock(Authentication.class);
        String url = "/ads/{adPk}/comments/{id}";
        CommentDTO commentDTO = getCommentDTO();
        CommentEntity commentEntity = commentMapper.toEntity(commentDTO);
        JSONObject commentDTOJSON = new JSONObject();
        commentDTOJSON.put("author", commentDTO.getAuthor());
        commentDTOJSON.put("createdAt", commentDTO.getCreatedAt());
        commentDTOJSON.put("pk", commentDTO.getPk());
        commentDTOJSON.put("text", commentDTO.getText());
        when(adsRepository.findById(anyInt())).thenReturn(Optional.of(getAdEntity()));
        when(adsService.updateComments(1,1, commentDTO, auth)).thenReturn(commentDTO);
        when(commentRepository.findByIdAndAd_Id(1, 1)).thenReturn(Optional.of(getCommentEntity()));
        when(userRepository.findById(1)).thenReturn(Optional.of(getNewAuthor()));
        when(commentRepository.save(commentEntity)).thenReturn(commentEntity);
        mockMvc.perform(patch(url, 1, 1)
                        .content(String.valueOf(commentDTOJSON))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    @WithMockUser(value = "user@gmail.com")
    void updateAds() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        Authentication auth = Mockito.mock(Authentication.class);
        String url = "/ads/{id}";
        CreateAds createAds = getCreateAds();
        JSONObject createAdsJSON =  new JSONObject();
        createAdsJSON.put("description", createAds.getDescription());
        createAdsJSON.put("price", createAds.getPrice());
        createAdsJSON.put("title", createAds.getTitle());


        when(adsRepository.findById(any())).thenReturn(Optional.of(getAdEntity()));

        AdEntity resultAdEntity = getAdEntity();
        resultAdEntity.setPrice(99);
        resultAdEntity.setDescription("описание");
        resultAdEntity.setTitle("заголовок");

        when(adsRepository.save(resultAdEntity)).thenReturn(resultAdEntity);
        when(adsService.updateAds(1, getCreateAds(), auth)).thenReturn(getAdsDTO());

        mockMvc.perform(patch(url, 1)
                        .content(createAdsJSON.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(value = "user@gmail.com")
    void getAdsMe() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        Authentication auth = Mockito.mock(Authentication.class);
        when(adsService.getAdsMe(auth)).thenReturn(getResponseWrapperAds());
        String url = "/ads/me";
        mockMvc.perform(get(url)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(value = "user@gmail.com")
    void getAdsById() throws Exception {
        Authentication auth = Mockito.mock(Authentication.class);
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        when(adsService.getAdById(1, auth)).thenReturn(getFullAds());
        String url = "/ads/{id}";
        mockMvc.perform(get(url, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(value = "user@gmail.com")
    void getAds() throws Exception {
        Authentication auth = Mockito.mock(Authentication.class);
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        when(adsService.getAds()).thenReturn(getResponseWrapperAds());
        String url = "/ads";
        mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk());
    }



    @Test
    @WithMockUser(value = "user@gmail.com")
    void getAdsComments() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        when(adsService.getAdsComments(1)).thenReturn(getResponseWrapperComment());
        String url = "/ads/{ad_pk}/comments";
        mockMvc.perform(get(url, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getComments() throws Exception {
        String url = "/ads/{ad_pk}/comments/{id}";
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        when(adsService.getComments(1, 1)).thenReturn(getCommentDTO());
        mockMvc.perform(get(url, 1, 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk());
    }

    private FullAds getFullAds() {
        return new FullAds("testName", "testLastName", "testDescription",
                "email@mail.ru", List.of("path/to/image"), "+79998887766",
                1, 100, "testTitle");
    }

    private ResponseWrapperAds getResponseWrapperAds() {
        AdsDTO adsDTO = getAdsDTO();
        return new ResponseWrapperAds(List.of(adsDTO).size(), List.of(adsDTO));
    }

    private AdsDTO getAdsDTO() {
        return new AdsDTO(1, "path/to/image", 1, 100, "testTitle");
    }

    private ResponseWrapperComment getResponseWrapperComment() {
        CommentDTO commentDTO = getCommentDTO();
        return new ResponseWrapperComment(List.of(commentDTO).size(), List.of(commentDTO));
    }

    private CommentDTO getCommentDTO() {
        return new CommentDTO(1, LocalDateTime.of(2023, 03,01,
            10, 00, 00).toString(), 1, "testText");
    }

    private CreateAds getCreateAds() {
        return new CreateAds("testDescription", 100, "testTitle");
    }

    private AdEntity getAdEntity() {
        AdEntity adEntity = new AdEntity();
        adEntity.setId(3);
        adEntity.setAuthor(getNewAuthor());
        List<ImageEntity> imageEntityList = new ArrayList<>();
        imageEntityList.add(new ImageEntity(1, "/ads/image/1", adEntity));
        imageEntityList.add(new ImageEntity(2, "/ads/image/2", adEntity));
        adEntity.setImageEntities(imageEntityList);
        return adEntity;
    }

    private UserEntity getNewAuthor() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1);
        return userEntity;
    }

    private ImageEntity getImageEntity() {
        return new ImageEntity(1, "path/to/image", getAdEntity());
    }

    private CommentEntity getCommentEntity() {
        return new CommentEntity(1, getNewAuthor(), LocalDateTime.of(2023, 03,01,
            10, 00, 00), getAdEntity(), "testText");
    }


}