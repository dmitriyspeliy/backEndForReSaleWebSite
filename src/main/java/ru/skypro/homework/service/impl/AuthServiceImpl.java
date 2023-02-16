package ru.skypro.homework.service.impl;

import javax.sql.DataSource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.RegisterReq;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.service.AuthService;
import ru.skypro.homework.service.UserService;

@Service
public class AuthServiceImpl implements AuthService {

  private final UserDetailsManager manager;
  private  JdbcUserDetailsManager jdbcUserDetailsManager;
  private final UserService userService;
  private final PasswordEncoder encoder;

  private final DataSource dataSource;

  public AuthServiceImpl(UserDetailsManager manager, UserService userService, DataSource dataSource) {
    this.manager = manager;
    this.userService = userService;
    this.dataSource = dataSource;
    this.encoder = new BCryptPasswordEncoder();
  }

  @Override
  public boolean login(String userName, String password) {
    if (!manager.userExists(userName)) {
      return false;
    }
    UserDetails userDetails = manager.loadUserByUsername(userName);
    String encryptedPassword = userDetails.getPassword();
    String encryptedPasswordWithoutEncryptionType = encryptedPassword.substring(8);
    return encoder.matches(password, encryptedPasswordWithoutEncryptionType);
  }

  @Override
  public boolean register(RegisterReq registerReq, Role role) {
    if (manager.userExists(registerReq.getUsername())) {
      return false;
    }
    jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);

    jdbcUserDetailsManager.createUser(
        User.withDefaultPasswordEncoder()
            .password(registerReq.getPassword())
            .username(registerReq.getUsername())

            .roles(role.name())
            .build()
    );
//    userService.addUser(registerReq, role);
    return true;
  }
}
