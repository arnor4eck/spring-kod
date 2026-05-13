package com.arnor4eck.springkod;

import com.arnor4eck.springkod.entity.user.Role;
import com.arnor4eck.springkod.entity.user.User;
import com.arnor4eck.springkod.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile({"dev"})
@AllArgsConstructor
public class CommandLineRunner implements org.springframework.boot.CommandLineRunner {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private record UserReq(String email, String username){}

    @Override
    public void run(String... args) {

        List<UserReq> userReqList = List.of(
                new UserReq("user1@mail.ru", "user1"),
                new UserReq("user2@mail.ru", "user2"),
                new UserReq("user3@mail.ru", "user3"),
                new UserReq("user4@mail.ru", "user4"),
                new UserReq("user5@mail.ru", "user5")
        );

        userRepository.saveAll(userReqList.stream()
                .map(uq -> User.builder()
                        .email(uq.email())
                        .role(Role.USER)
                        .username(uq.username())
                        .password(passwordEncoder.encode("password"))
                        .build())
                .toList());
    }
}

