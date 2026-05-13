package com.arnor4eck.springkod.service;

import com.arnor4eck.springkod.entity.user.User;
import com.arnor4eck.springkod.repository.UserRepository;
import com.arnor4eck.springkod.util.jwt.JwtAccessUtils;
import com.arnor4eck.springkod.util.request.AuthenticationRequest;
import com.arnor4eck.springkod.util.request.CreateUserRequest;
import com.arnor4eck.springkod.util.response.AuthenticationResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final JwtAccessUtils jwtUtils;

    private final AuthenticationManager manager;

    private final PasswordEncoder passwordEncoder;

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest){
        try{
            Authentication authentication = manager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.email(),
                            authenticationRequest.password()));

            String token = jwtUtils.generateToken((User) authentication.getPrincipal());

            return new AuthenticationResponse(token);
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    e.getMessage());
        }
    }

    public User registration(CreateUserRequest createUserRequest){
        boolean isUserWithSameEmailExists = userRepository.existsByEmail(createUserRequest.email());

        if(isUserWithSameEmailExists){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Пользователь с таким email уже существует.");
        }else{
            User newUser = User.builder()
                    .email(createUserRequest.email())
                    .password(passwordEncoder.encode(createUserRequest.password()))
                    .username(createUserRequest.username())
                    .build();

            return userRepository.save(newUser);
        }
    }

}
