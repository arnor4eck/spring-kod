package com.arnor4eck.springkod.controller;

import com.arnor4eck.springkod.service.DatasitoryService;
import com.arnor4eck.springkod.util.dto.datasitory.DatasitoryDto;
import com.arnor4eck.springkod.util.request.datasitory.CreateDatasitoryRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}") // TODO проверка на то что создатель или в мемберах
    public ResponseEntity<@NonNull DatasitoryDto> getDatasitoryById(@PathVariable long id) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new DatasitoryDto(
                        datasitoryService.getById(id)));
    }

    @GetMapping("/my")
    public ResponseEntity<@NonNull Iterable<DatasitoryDto>> getAllUserDatasitory(@AuthenticationPrincipal String email) {
        return ResponseEntity.accepted().body(datasitoryService.getAllDatasitoriesByUserEmail(email)
                .stream()
                .map(DatasitoryDto::new)
                .toList());
    }
}
