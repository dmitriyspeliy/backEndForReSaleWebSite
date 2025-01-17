package ru.skypro.homework.service.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.exception.ElemNotFound;
import ru.skypro.homework.repository.UserRepository;

@Service
public class SecurityService {

    private UserRepository userRepository;

    public SecurityService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    /** Проверка пользователя на авторство */
    public boolean checkAuthor(int id, UserEntity user) {
        return id == user.getId();
    }
    /** Проверка автора объявления на электронную почту */
    public boolean checkAuthor(int id, String email) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(ElemNotFound::new);
        return checkAuthor(id, user);
    }
    /** Проверка пользователя на электронную почту */
    public boolean isAuthorAuthenticated(String email, Authentication authentication) {
        return authentication.getName().equals(email) && authentication.isAuthenticated();
    }

    public boolean isAuthorAuthenticated(int id, Authentication authentication) {
        UserEntity user = userRepository.findById(id).orElseThrow(ElemNotFound::new);
        return isAuthorAuthenticated(user.getEmail(), authentication);
    }

    public boolean isAuthorAuthenticated(UserEntity user, Authentication authentication) {
        return isAuthorAuthenticated(user.getEmail(), authentication);
    }
    /** Проверка пользователя на роль администратора */
    public boolean isAdmin(UserEntity user) {
        return user.getRole().equals(Role.ADMIN);
    }

    public boolean isAdmin(int id) {
        UserEntity user = userRepository.findById(id).orElseThrow(ElemNotFound::new);
        return isAdmin(user);
    }

    public boolean isAdmin(String email) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(ElemNotFound::new);
        return isAdmin(user);
    }

    public boolean isAdmin(Authentication authentication) {
        return authentication.isAuthenticated() &&
                authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
    /** Проверка законности доступа к методам комментариям */
    public boolean isCommentUpdateAvailable(Authentication authentication, int commentEntityAuthorId,
                                            int commentDTOAuthorId) {
        if (isUpdateAvailable(authentication)) {
            return true;
        }
        if (checkAuthor(commentEntityAuthorId, authentication.getName()) &&
                commentEntityAuthorId == commentDTOAuthorId) {
            return true;
        }
        return false;
    }

    /** Проверка законности доступа к методам объявлений */
    public boolean isAdsUpdateAvailable(Authentication authentication, int adEntityAuthorId) {
        if (isUpdateAvailable(authentication)) {
            return true;
        }
        if (checkAuthor(adEntityAuthorId, authentication.getName())) {
            return true;
        }
        return false;
    }

    /** Проверка возможности обновления */
    private boolean isUpdateAvailable(Authentication authentication) {
        if (!authentication.isAuthenticated()) {
            return false;
        }
        if (isAdmin(authentication)) {
            return true;
        }
        return false;
    }
}
