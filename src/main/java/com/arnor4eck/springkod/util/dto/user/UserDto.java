package com.arnor4eck.springkod.util.dto.user;

import com.arnor4eck.springkod.entity.user.User;

import java.time.format.DateTimeFormatter;

public record UserDto(long id, String email,
                      String username, String createdAt) {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public UserDto(User user){
        this(user.getId(), user.getEmail(),
            user.getUsername(), formatter.format(user.getCreatedAt()));
    }
}
