package com.arnor4eck.springkod.controller;

import com.arnor4eck.springkod.entity.datasitory_file.FileType;
import com.arnor4eck.springkod.service.DatasitoryService;
import com.arnor4eck.springkod.service.FileService;
import com.arnor4eck.springkod.util.dto.file.FileUrlDto;
import com.arnor4eck.springkod.util.file.FileImpl;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@AllArgsConstructor
public class FileController {

    private final FileService fileService;

    private final DatasitoryService datasitoryService;

    @PostMapping("/images/{id}")
    @PreAuthorize("@datasitoryService.isOwner(authentication, #datasitoryId)")
    public ResponseEntity<@NonNull Void> saveImages(@PathVariable("id") long datasitoryId,
                                                   @RequestParam("files") List<MultipartFile> files) {
        fileService.saveImages(files, datasitoryId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/images/{id}")
    @PreAuthorize("@datasitoryService.hasAccess(authentication, #datasitoryId)")
    public ResponseEntity<@NonNull List<FileUrlDto>> getImages(@PathVariable("id") long datasitoryId) {
        return ResponseEntity.ok(fileService.loadImages(datasitoryId)
                .stream()
                .map(FileUrlDto::new)
                .toList());
    }

    @GetMapping("/image/{name}")
    public ResponseEntity<@NonNull Resource> getImage(@PathVariable("name") String name) throws IOException {
        FileImpl file = fileService.loadFile(name);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getOriginalFilename() + "\"")
                .body(new ByteArrayResource(file.getBytes()));
    }

    @PostMapping("/probability/{id}")
    @PreAuthorize("@datasitoryService.hasAccess(authentication, #datasitoryId)")
    public ResponseEntity<@NonNull Void> saveProbability(@PathVariable("id") long datasitoryId,
                                                        @RequestParam("file") MultipartFile file) {
        fileService.saveFile(file, datasitoryId, FileType.PROBABILITY);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/probability/{id}")
    @PreAuthorize("@datasitoryService.hasAccess(authentication, #datasitoryId)")
    public ResponseEntity<@NonNull Resource> getProbability(@PathVariable("id") long datasitoryId) throws IOException {
        return inlineFile(fileService.loadFile(datasitoryId, FileType.PROBABILITY));
    }

    @PostMapping("/metadata/{id}")
    @PreAuthorize("@datasitoryService.hasAccess(authentication, #datasitoryId)")
    public ResponseEntity<@NonNull Void> saveMetadata(@PathVariable("id") long datasitoryId,
                                                    @RequestParam("file") MultipartFile file) {
        fileService.saveFile(file, datasitoryId, FileType.METADATA);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/metadata/{id}")
    @PreAuthorize("@datasitoryService.hasAccess(authentication, #datasitoryId)")
    public ResponseEntity<@NonNull Resource> getMetadata(@PathVariable("id") long datasitoryId) throws IOException {
        return inlineFile(fileService.loadFile(datasitoryId, FileType.METADATA));
    }

    @PostMapping("/markup/{id}")
    @PreAuthorize("@datasitoryService.hasAccess(authentication, #datasitoryId)")
    public ResponseEntity<@NonNull Void> saveMarkup(@PathVariable("id") long datasitoryId,
                                                      @RequestParam("file") MultipartFile file) {
        fileService.saveFile(file, datasitoryId, FileType.MARKUP_FILE);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/markup/{id}")
    @PreAuthorize("@datasitoryService.hasAccess(authentication, #datasitoryId)")
    public ResponseEntity<@NonNull Resource> getMarkup(@PathVariable("id") long datasitoryId) throws IOException {
        return inlineFile(fileService.loadFile(datasitoryId, FileType.MARKUP_FILE));
    }

    private ResponseEntity<@NonNull Resource> inlineFile(FileImpl file) throws IOException {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getOriginalFilename() + "\"")
                .body(new ByteArrayResource(file.getBytes()));
    }
}
