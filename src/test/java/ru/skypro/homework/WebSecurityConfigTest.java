package ru.skypro.homework;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import ru.skypro.homework.utils.Encoder;

@TestConfiguration
public class WebSecurityConfigTest {

  private static final String[] AUTH_WHITELIST = {
      "/swagger-resources/**",
      "/swagger-ui.html",
      "/v3/api-docs",
      "/webjars/**",
      "/login", "/register",
      "/ads/*",
      "/users/*"

  };

  @Bean
  public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
//    UserDetails user = User.withDefaultPasswordEncoder()
    UserDetails user = User.withUsername("user@gmail.com")
//        .username("user@gmail.com")
//        .password("password")
        .password(passwordEncoder.encode("password"))
        .roles("USER")
        .build();
    return new InMemoryUserDetailsManager(user);
  }
  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
  private final UserDetailsService userDetailService;

  public WebSecurityConfigTest(UserDetailsService userDetailService) {
    this.userDetailService = userDetailService;
  }

  @Bean
  protected DaoAuthenticationProvider daoAuthenticationProvider() {
    DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
    daoAuthenticationProvider.setPasswordEncoder(Encoder.passwordEncoderWithBCrypt());
    daoAuthenticationProvider.setUserDetailsService(userDetailService);
    return daoAuthenticationProvider;
  }

  @Bean
  protected AuthenticationManagerBuilder authenticationManagerBuilder(
      AuthenticationManagerBuilder auth
  ) {
    return auth.authenticationProvider(daoAuthenticationProvider());
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        .authorizeHttpRequests((authz) ->
        {
          try {
            authz
                .mvcMatchers(AUTH_WHITELIST).permitAll()
                .mvcMatchers(HttpMethod.GET, "/ads").permitAll()
                .mvcMatchers("/ads/**", "/users/**")
                .authenticated();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        })
        .cors().and()
        .httpBasic(withDefaults());
    return http.build();
  }


}

