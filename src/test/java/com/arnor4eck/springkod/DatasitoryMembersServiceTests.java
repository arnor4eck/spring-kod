package com.arnor4eck.springkod;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;
import com.arnor4eck.springkod.entity.datasitory.DatasitoryType;
import com.arnor4eck.springkod.entity.datasitory_member.DatasitoryMember;
import com.arnor4eck.springkod.entity.datasitory_member.DatasitoryMemberRole;
import com.arnor4eck.springkod.entity.user.User;
import com.arnor4eck.springkod.repository.DatasitoryMemberRepository;
import com.arnor4eck.springkod.repository.DatasitoryRepository;
import com.arnor4eck.springkod.service.DatasitoryMembersService;
import com.arnor4eck.springkod.util.exception.DatasitoryNotFoundException;
import com.arnor4eck.springkod.util.exception.UserNotFoundException;
import com.arnor4eck.springkod.util.request.AddMemberToDatasitoryRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DatasitoryMembersServiceTests {
    @InjectMocks
    private DatasitoryMembersService datasitoryMembersService;

    @Mock
    private DatasitoryMemberRepository datasitoryMemberRepository;
    @Mock
    private DatasitoryRepository datasitoryRepository;
    @Mock
    private UserDetailsService userDetailsService;

    @Test
    public void testAddMemberSuccess(){
        String memberEmail = "member@mail.ru";
        long datasitoryId = 1L;

        AddMemberToDatasitoryRequest request = createRequest(memberEmail,
                DatasitoryMemberRole.ANALYST);
        Datasitory datasitory = createDatasitory(createUser("owner@mail.ru"));
        User newMember = createUser(memberEmail);

        when(datasitoryRepository.findById(anyLong()))
                .thenReturn(Optional.of(datasitory));
        when(userDetailsService.loadUserByUsername(anyString()))
                .thenReturn(newMember);
        when(datasitoryMemberRepository.save(any()))
                .thenAnswer(incov -> incov.getArgument(0));

        DatasitoryMember member = datasitoryMembersService.addMember(datasitoryId, request);

        Assertions.assertNotNull(member);
        Assertions.assertEquals(memberEmail, member.getUser().getEmail());
        Assertions.assertEquals(datasitory, member.getDatasitory());
    }

    @Test
    public void testAddMemberDatasitoryNotFound(){
        long datasitoryId = 1;

        AddMemberToDatasitoryRequest request = createRequest("member@mail.ru",
                DatasitoryMemberRole.ANALYST);

        when(datasitoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        DatasitoryNotFoundException ex = Assertions.assertThrows(DatasitoryNotFoundException.class,
                () ->  datasitoryMembersService.addMember(datasitoryId, request));

        Assertions.assertEquals(ex.getMessage(),
                String.format("Датазитория с id %d нет.", datasitoryId));
    }

    @Test
    public void testAddMemberMemberNotFound(){
        AddMemberToDatasitoryRequest request = createRequest("member@mail.ru",
                DatasitoryMemberRole.ANALYST);

        Datasitory datasitory = createDatasitory(createUser("owner@mail.ru"));

        when(datasitoryRepository.findById(anyLong()))
                .thenReturn(Optional.of(datasitory));
        when(userDetailsService.loadUserByUsername(anyString()))
                .thenThrow(new UserNotFoundException("Пользователь не найден"));

        Assertions.assertThrows(UserNotFoundException.class,
                () ->  datasitoryMembersService.addMember(1L, request));
    }

    private AddMemberToDatasitoryRequest createRequest(String email,
                                                       DatasitoryMemberRole role){
        return new AddMemberToDatasitoryRequest(email, role.toString());
    }

    private Datasitory createDatasitory(User creator){
        return Datasitory.builder()
                .id(1L)
                .name("datasitory")
                .description("description")
                .datasitoryType(DatasitoryType.OPEN)
                .creator(creator)
                .createdAt(LocalDateTime.MAX)
                .updatedAt(LocalDateTime.MAX)
                .build();
    }

    private User createUser(String email){
        return User.builder()
                .username("username")
                .password("password")
                .createdAt(LocalDateTime.MAX)
                .email(email)
                .build();
    }
}
