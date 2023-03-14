package ru.skypro.homework.controller;


import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.skypro.homework.WebSecurityConfigTest;
import ru.skypro.homework.repository.AdsRepository;
import ru.skypro.homework.repository.ImageRepository;
import ru.skypro.homework.security.UserDetailServiceImpl;
import ru.skypro.homework.service.AdsService;
import ru.skypro.homework.service.ImageService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImageController.class)
@Import(value = WebSecurityConfigTest.class)
class ImageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext context;

  @InjectMocks
  private ImageController imageController;

  @MockBean
  private AdsService adsService;

  @MockBean
  private AdsRepository adsRepository;

  @MockBean
  private ImageService imageService;

  @MockBean
  private ImageRepository imageRepository;

  @MockBean
  private UserDetailServiceImpl userDetailsService;

  @Test
  public void contextLoads() {
    assertNotNull(imageController);
    assertThat(imageController).isNotNull();
  }

  @Test
  @WithMockUser(value = "user@gmail.com")
  public void imageControllerTest() throws Exception {

    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    MockMultipartFile image = new MockMultipartFile("image", "image.jpeg",
        MediaType.IMAGE_JPEG_VALUE, "image.jpeg".getBytes());
    when(adsService.getPhotoById(anyInt())).thenReturn(image.getBytes());

    mockMvc.perform(multipart(HttpMethod.PATCH, "/ads/{id}/image", 1)
            .file(image)
            .with(user("user@gmail.com").password("password").roles("USER"))
//            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .accept(MediaType.MULTIPART_FORM_DATA_VALUE))
        .andDo(print())
        .andExpect(status().isOk());
    mockMvc.perform(get("/ads/{id}", 1)
//            .with(user("user@gmail.com").password("password").roles("USER"))
        .contentType(MediaType.IMAGE_PNG_VALUE).accept(MediaType.IMAGE_PNG_VALUE))
        .andDo(print())
        .andExpect(status().isOk());
  }
}