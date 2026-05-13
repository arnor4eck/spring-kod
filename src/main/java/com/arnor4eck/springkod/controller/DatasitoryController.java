package com.arnor4eck.springkod.controller;

import com.arnor4eck.springkod.service.DatasitoryService;
import com.arnor4eck.springkod.util.dto.datasitory.DatasitoryDto;
import com.arnor4eck.springkod.util.request.datasitory.CreateDatasitoryRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
