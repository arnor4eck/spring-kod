package com.arnor4eck.springkod.controller;

import com.arnor4eck.springkod.service.DatasitoryService;
import com.arnor4eck.springkod.util.dto.datasitory.DatasitoryDto;
import com.arnor4eck.springkod.util.dto.datasitory_member.DatasitoryMemberDto;
import com.arnor4eck.springkod.util.request.AddMemberToDatasitoryRequest;
import com.arnor4eck.springkod.util.request.datasitory.CreateDatasitoryRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileNotFoundException;

@RestController
@RequestMapping("/api/v1/datasitory")
@AllArgsConstructor
public class DatasitoryController {

    private final DatasitoryService datasitoryService;

    @PostMapping
    public ResponseEntity<@NonNull DatasitoryDto> createDatasitory(@RequestBody @Valid CreateDatasitoryRequest createDatasitoryRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new DatasitoryDto(
                        datasitoryService.createDatasitory(createDatasitoryRequest)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@datasitoryService.hasAccess(authentication, #datasitoryId)")
    public ResponseEntity<@NonNull DatasitoryDto> getDatasitoryById(@PathVariable("id") long datasitoryId) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new DatasitoryDto(
                        datasitoryService.getById(datasitoryId)));
    }

    @GetMapping("/my")
    public ResponseEntity<@NonNull Iterable<DatasitoryDto>> getAllUserDatasitory(@AuthenticationPrincipal String email) {
        return ResponseEntity.accepted()
                .body(datasitoryService.getAllDatasitoriesByUserEmail(email)
                .stream()
                .map(DatasitoryDto::new)
                .toList());
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<@NonNull Iterable<DatasitoryDto>> getAllUserDatasitories(@PathVariable("id") long userId) {
        return ResponseEntity.accepted()
                .body(datasitoryService.getAllDatasitoriesByUserIdExpectPrivate(userId)
                    .stream()
                    .map(DatasitoryDto::new)
                    .toList());
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("@datasitoryService.isOwner(authentication, #datasitoryId)")
    public ResponseEntity<@NonNull DatasitoryMemberDto> addMember(@PathVariable("id") long datasitoryId,
                                                                  @RequestBody @Valid AddMemberToDatasitoryRequest request){
        return ResponseEntity.ok(new DatasitoryMemberDto(
                datasitoryService.addMember(datasitoryId, request))
        );
    }

    @GetMapping("/export/{id}")
    @PreAuthorize("@datasitoryService.hasAccess(authentication, #datasitoryId)")
    public ResponseEntity<@NonNull StreamingResponseBody> exportDatasitory(@PathVariable("id") long datasitoryId) throws FileNotFoundException {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=dataset_" + datasitoryId + ".zip")
                .body(datasitoryService.export(datasitoryId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@datasitoryService.isOwner(authentication, #datasitoryId)")
    public ResponseEntity<@NonNull Void> deleteDatasitoryById(@PathVariable("id") long datasitoryId) {
        datasitoryService.delete(datasitoryId);

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
