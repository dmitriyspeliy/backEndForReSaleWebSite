package ru.skypro.homework.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
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
import ru.skypro.homework.dto.AdsDTO;
import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.UserDTO;
import ru.skypro.homework.repository.AdsRepository;
import ru.skypro.homework.repository.ImageRepository;
import ru.skypro.homework.security.UserDetailServiceImpl;
import ru.skypro.homework.service.AdsService;
import ru.skypro.homework.service.ImageService;
import ru.skypro.homework.service.UserService;

//@WebMvcTest(UserController.class)
@Import(value = WebSecurityConfigTest.class)
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {


  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext context;

  @InjectMocks
  private UserController userController;

  @MockBean
  private UserDetailServiceImpl userDetailsService;

  @MockBean
  private UserService userService;



  @Test
  public void contextLoads() {
    assertNotNull(userController);
  }

  @Test
  @WithMockUser(value = "user@gmail.com")
  public void userControllerTest() throws Exception {
    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    Authentication auth = Mockito.mock(Authentication.class);
    NewPassword newPassword = getNewPassword();
    JSONObject newPasswordJSON =  new JSONObject();
    newPasswordJSON.put("id", newPassword.getId());
    newPasswordJSON.put("currentPassword", newPassword.getCurrentPassword());
    newPasswordJSON.put("pk", newPassword.getNewPassword());
    UserDTO userDTO = getUserDTO();
    JSONObject userDTOJSON =  new JSONObject();
    userDTOJSON.put("email", userDTO.getEmail());
    userDTOJSON.put("firstName", userDTO.getFirstName());
    userDTOJSON.put("id", userDTO.getId());
    userDTOJSON.put("lastName", userDTO.getLastName());
    userDTOJSON.put("phone", userDTO.getPhone());
    userDTOJSON.put("regDate", userDTO.getRegDate());
    userDTOJSON.put("city", userDTO.getCity());
    userDTOJSON.put("image", userDTO.getImage());
    MockMultipartFile image = new MockMultipartFile("image", "image.jpeg",
        MediaType.IMAGE_JPEG_VALUE, "image.jpeg".getBytes());
    when(userService.getUser(auth)).thenReturn(getUserDTO());
    when(userService.updateUser(getUserDTO(),auth)).thenReturn(getUserDTO());
    doNothing().when(userService).updateUserImage(image, auth);
    mockMvc.perform(post("/users/setPassword")
            .content(String.valueOf(newPasswordJSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isOk());
    mockMvc.perform(get("/users/me")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isOk());
    mockMvc.perform(patch("/users/me")
            .content(String.valueOf(userDTOJSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isOk());
    mockMvc.perform(multipart(HttpMethod.PATCH, "/users/me/image")
            .file(image)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .accept(MediaType.MULTIPART_FORM_DATA_VALUE))
        .andDo(print())
        .andExpect(status().isOk());
  }

  private NewPassword getNewPassword() {
    return new NewPassword(1L, "oldPassword", "newPassword");
  }
  private UserDTO getUserDTO() {
    return new UserDTO("dmitry@gmail.com"
        , "Dmitry", 1, "Pospelov"
        , "89299129121", "20-02-2023 10:12:13", "Moscow", "Реклама");
  }
}