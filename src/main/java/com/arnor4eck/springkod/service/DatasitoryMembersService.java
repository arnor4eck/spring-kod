package com.arnor4eck.springkod.service;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;
import com.arnor4eck.springkod.entity.datasitory_member.DatasitoryMember;
import com.arnor4eck.springkod.entity.datasitory_member.DatasitoryMemberRole;
import com.arnor4eck.springkod.entity.user.User;
import com.arnor4eck.springkod.repository.DatasitoryMemberRepository;
import com.arnor4eck.springkod.repository.DatasitoryRepository;
import com.arnor4eck.springkod.util.exception.DatasitoryNotFoundException;
import com.arnor4eck.springkod.util.request.AddMemberToDatasitoryRequest;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DatasitoryMembersService {

    private final DatasitoryMemberRepository datasitoryMemberRepository;

    private final DatasitoryRepository datasitoryRepository;

    private final UserDetailsService userDetailsService;

    public DatasitoryMember addMember(AddMemberToDatasitoryRequest addMemberToDatasitoryRequest){
        Datasitory datasitory = findDatasitoryByIdOrThrow(addMemberToDatasitoryRequest.datasitoryId());
        User member = findUserByEmailOrThrow(addMemberToDatasitoryRequest.memberEmail());

        DatasitoryMemberRole memberRole = DatasitoryMemberRole
                .valueOf(addMemberToDatasitoryRequest.datasitoryRole().toUpperCase());

        DatasitoryMember datasitoryMember = DatasitoryMember.builder()
                .datasitory(datasitory)
                .user(member)
                .datasitoryMemberRole(memberRole)
                .build();

        return datasitoryMemberRepository.save(datasitoryMember);
    }

    private Datasitory findDatasitoryByIdOrThrow(long datasitoryId){
        return datasitoryRepository.findById(datasitoryId)
                .orElseThrow(() ->
                        new DatasitoryNotFoundException("Датазитория с id %d нет."
                                .formatted(datasitoryId)));
    }

    private User findUserByEmailOrThrow(String email){
        return (User) userDetailsService.loadUserByUsername(email);
    }
}
