package com.vini.agendaai.service;

import com.vini.agendaai.model.User;
import com.vini.agendaai.repository.UserRepository;
import com.vini.agendaai.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return UserPrincipal.create(user);
    }

    @Transactional
    public User processOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");
        String profilePictureUrl = oAuth2User.getAttribute("picture");

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Atualizar informações do usuário existente
            user.setName(name);
            user.setProfilePictureUrl(profilePictureUrl);
            user.setGoogleId(googleId);
            user.setUpdatedAt(LocalDateTime.now());
        } else {
            // Criar novo usuário
            user = User.builder()
                    .email(email)
                    .name(name)
                    .googleId(googleId)
                    .profilePictureUrl(profilePictureUrl)
                    .isActive(true)
                    .build();
        }

        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User updateUserTokens(User user, String accessToken, String refreshToken, LocalDateTime tokenExpiry) {
        user.setAccessToken(accessToken);
        user.setRefreshToken(refreshToken);
        user.setTokenExpiry(tokenExpiry);
        return userRepository.save(user);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
