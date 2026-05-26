package com.arnor4eck.springkod.service;

import com.arnor4eck.springkod.config.rest_template.RestTemplateVariables;
import com.arnor4eck.springkod.entity.datasitory_file.FileType;
import com.arnor4eck.springkod.util.ResponseTransformer;
import com.arnor4eck.springkod.util.exception.FileNotFoundInStorageException;
import com.arnor4eck.springkod.util.file.FileImpl;
import com.arnor4eck.springkod.util.file.loader.FileLoader;
import com.arnor4eck.springkod.util.response.ml.MlAnalyticsResponse;
import com.arnor4eck.springkod.util.response.ml.to_frontend.MlAnalyticsResponseWithUrls;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class MlService {

    private final RestTemplate restTemplate;

    private final RestTemplateVariables restTemplateVariables;

    private final FileLoader fileLoader;

    private final ResponseTransformer responseTransformer;

    public MlAnalyticsResponseWithUrls getMlAnalitics(long datasitoryId) throws IOException, FileNotFoundInStorageException {

        List<FileImpl> allFiles = fileLoader.loadAll(datasitoryId);

        List<FileImpl> images = getAllImages(allFiles);
        FileImpl markupFile = getMarkUpFile(allFiles);
        Optional<FileImpl> probabilityFile = getProbabilityFile(allFiles);
        Optional<FileImpl> metadataFile = getMetadataFile(allFiles);

        return responseTransformer.transformToFrontendResponse(datasitoryId, analitics(images, markupFile, probabilityFile, metadataFile));
    }

    private FileImpl getMarkUpFile(List<FileImpl> allFiles) throws FileNotFoundInStorageException {
        FileImpl markUpFile = getSoloFileByType(allFiles, FileType.MARKUP_FILE);

        if(markUpFile == null){
            throw new FileNotFoundInStorageException("Нет обязательного файла разметки");
        }

        return markUpFile;
    }

    private Optional<FileImpl> getProbabilityFile(List<FileImpl> allFiles) {
        return Optional.ofNullable(getSoloFileByType(allFiles, FileType.PROBABILITY));
    }

    private Optional<FileImpl> getMetadataFile(List<FileImpl> allFiles) {
        return Optional.ofNullable(getSoloFileByType(allFiles, FileType.METADATA));
    }

    private List<FileImpl> getAllImages(List<FileImpl> allFiles) {
        return allFiles.stream()
                .filter(i -> i.getFileType() == FileType.IMAGE)
                .toList();
    }

    private FileImpl getSoloFileByType(List<FileImpl> allFiles, FileType fileType) {
        for(FileImpl file : allFiles) {
            if(file.getFileType() == fileType)
                return file;
        }

        return null;
    }

    private MlAnalyticsResponse analitics(List<FileImpl> images,
                                         FileImpl markupFile,
                                         Optional<FileImpl> probability,
                                         Optional<FileImpl> metadata) throws IOException {
        String url = restTemplateVariables.mlServiceUrl() + "/upload/analitics";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        for (FileImpl image : images)
            addFile("images", image, body);

        addFile("markup_file", markupFile, body);

        addOptionalFile("probability", probability, body);
        addOptionalFile("metadata", metadata, body);

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        ResponseEntity<MlAnalyticsResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                MlAnalyticsResponse.class
        );

        return response.getBody();
    }

    private void addOptionalFile(String name, Optional<FileImpl> file,
                                 MultiValueMap<String, Object> body) throws IOException {
        if (file.isPresent()) {
            addFile(name, file.get(), body);
        }
        log.info("Файл {} не передан, порпуск", name);
    }

    private void addFile(String name, FileImpl file,
                         MultiValueMap<String, Object> body) throws IOException {
        body.add(name, createByteArrayResource(file));
    }

    private ByteArrayResource createByteArrayResource(FileImpl file) throws IOException {
        return new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
    }
}
