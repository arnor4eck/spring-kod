package com.arnor4eck.springkod;

import com.arnor4eck.springkod.entity.user.User;
import com.arnor4eck.springkod.repository.UserRepository;
import com.arnor4eck.springkod.service.UserService;
import com.arnor4eck.springkod.util.jwt.JwtAccessUtils;
import com.arnor4eck.springkod.util.request.AuthenticationRequest;
import com.arnor4eck.springkod.util.request.CreateUserRequest;
import com.arnor4eck.springkod.util.response.AuthenticationResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtAccessUtils jwtUtils;

    @Mock
    private AuthenticationManager manager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void testRegistrationSuccess(){
        CreateUserRequest createUserRequest = createUserRequest();


        prepareUserWithSameEmailExists(false);
        when(passwordEncoder.encode(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        User registeredUser = userService.registration(createUserRequest);

        Assertions.assertNotNull(registeredUser);
        Assertions.assertEquals(registeredUser.getUsername(), createUserRequest.username());
    }

    @Test
    public void testRegistrationUserAlreadyExists(){
        CreateUserRequest createUserRequest = createUserRequest();

        prepareUserWithSameEmailExists(true);

        ResponseStatusException ex = Assertions.assertThrows(ResponseStatusException.class,
                () -> userService.registration(createUserRequest));
        Assertions.assertEquals("Пользователь с таким email уже существует.", ex.getReason());
    }

    @Test
    public void testAuthenticationSuccess(){
        AuthenticationRequest authenticationRequest = authenticationRequest();

        Authentication authentication = Mockito.mock(Authentication.class);
        when(manager.authenticate(any())).thenReturn(authentication);
        when(jwtUtils.generateToken(any())).thenReturn("token");

        AuthenticationResponse authenticationResponse = userService.authenticate(authenticationRequest);

        Assertions.assertNotNull(authenticationResponse.token());
    }

    @Test
    public void testAuthenticationUserNotFound(){
        AuthenticationRequest authenticationRequest = authenticationRequest();

        when(manager.authenticate(any())).thenThrow(new InternalAuthenticationServiceException("Пользователь с email test@mail.ru не найден."));

        ResponseStatusException ex = Assertions.assertThrows(ResponseStatusException.class, () -> userService.authenticate(authenticationRequest));

        Assertions.assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatusCode());
    }

    private CreateUserRequest createUserRequest(){
        return new CreateUserRequest(
                "username",
                "user@mail.ru",
                "password"
        );
    }

    private void prepareUserWithSameEmailExists(boolean isExists){
        when(userRepository.existsByEmail(anyString())).thenReturn(isExists);
    }

    private AuthenticationRequest authenticationRequest(){
        return new AuthenticationRequest("user@mail.ru", "password");
    }
}
