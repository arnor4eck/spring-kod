package com.arnor4eck.springkod.util.dto.datasitory_member;

import com.arnor4eck.springkod.entity.datasitory_member.DatasitoryMember;
import com.arnor4eck.springkod.util.dto.user.UserDto;

import java.time.format.DateTimeFormatter;

public record DatasitoryMemberDto(UserDto user, String datasitoryMemberRole,
                                  String joinedAt) {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public DatasitoryMemberDto(DatasitoryMember member){
        this(new UserDto(member.getUser()),
            member.getDatasitoryMemberRole().toString(),
            formatter.format(member.getJoinedAt()));
    }
}
