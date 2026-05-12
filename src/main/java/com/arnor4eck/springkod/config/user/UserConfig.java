package com.arnor4eck.springkod.config.user;

import com.arnor4eck.springkod.repository.UserRepository;
import com.arnor4eck.springkod.util.exception.UserNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@AllArgsConstructor
public class UserConfig {

    private final UserRepository userRepository;

    @Bean
    public UserDetailsService userDetailsService(){
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String email) throws UserNotFoundException {
                return userRepository.findByEmail(email)
                        .orElseThrow(() -> new UserNotFoundException("Пользователь с email %s не найден.".formatted(email)));
            }
        };
    }
}
