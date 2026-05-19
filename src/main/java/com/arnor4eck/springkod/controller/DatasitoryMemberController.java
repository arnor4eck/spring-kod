package com.arnor4eck.springkod.controller;

import com.arnor4eck.springkod.service.DatasitoryMembersService;
import com.arnor4eck.springkod.util.dto.datasitory_member.DatasitoryMemberDto;
import com.arnor4eck.springkod.util.request.AddMemberToDatasitoryRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@AllArgsConstructor
public class DatasitoryMemberController {

    private final DatasitoryMembersService datasitoryMembersService;

    @PostMapping("/{id}") // TODO TEST IT
    public ResponseEntity<@NonNull DatasitoryMemberDto> addMember(@PathVariable long id,
                                                                   @RequestBody @Valid AddMemberToDatasitoryRequest request){
        return ResponseEntity.ok(new DatasitoryMemberDto(
                datasitoryMembersService.addMember(id, request))
        );
    }
}
