package com.arnor4eck.springkod.controller;

import com.arnor4eck.springkod.entity.datasitory_file.FileType;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@AllArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/images/{id}")
    public ResponseEntity<@NonNull Void> saveImages(@PathVariable("id") long id,
                                                   @RequestParam("files") List<MultipartFile> files) {
        fileService.saveImages(files, id);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/images/{id}")
    public ResponseEntity<@NonNull List<FileUrlDto>> getImages(@PathVariable("id") long id) {
        return ResponseEntity.ok(fileService.loadImages(id)
                .stream()
                .map(FileUrlDto::new)
                .toList());
    }

    @GetMapping("/image/{name}")
    public ResponseEntity<@NonNull Resource> getImage(@PathVariable("name") String name) throws IOException {
        FileImpl file = fileService.loadFile(name);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .contentType(MediaType.parseMediaType(file.getContentType())) // TODO
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getOriginalFilename() + "\"")
                .body(new ByteArrayResource(file.getBytes()));
    }

    @PostMapping("/probability/{id}")
    public ResponseEntity<@NonNull Void> saveProbability(@PathVariable("id") long id,
                                                    @RequestParam("file") MultipartFile file) {
        fileService.saveFile(file, id, FileType.PROBABILITY);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/probability/{id}")
    public ResponseEntity<@NonNull Resource> getProbability(@PathVariable("id") long id) throws IOException {
        return inlineFile(fileService.loadFile(id, FileType.PROBABILITY));
    }

    @PostMapping("/metadata/{id}")
    public ResponseEntity<@NonNull Void> saveMetadata(@PathVariable("id") long id,
                                                    @RequestParam("file") MultipartFile file) {
        fileService.saveFile(file, id, FileType.METADATA);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/metadata/{id}")
    public ResponseEntity<@NonNull Resource> getMetadata(@PathVariable("id") long id) throws IOException {
        return inlineFile(fileService.loadFile(id, FileType.METADATA));
    }

    @PostMapping("/markup/{id}")
    public ResponseEntity<@NonNull Void> saveMarkup(@PathVariable("id") long id,
                                                      @RequestParam("file") MultipartFile file) {
        fileService.saveFile(file, id, FileType.MARKUP_FILE);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/markup/{id}")
    public ResponseEntity<@NonNull Resource> getMarkup(@PathVariable("id") long id) throws IOException {
        return inlineFile(fileService.loadFile(id, FileType.MARKUP_FILE));
    }

    private ResponseEntity<@NonNull Resource> inlineFile(FileImpl file) throws IOException {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getOriginalFilename() + "\"")
                .body(new ByteArrayResource(file.getBytes()));
    }
}
