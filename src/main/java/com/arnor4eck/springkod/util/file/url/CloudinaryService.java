package com.arnor4eck.springkod.util.file.url;

import com.arnor4eck.springkod.entity.datasitory_file.DatasitoryFile;
import com.arnor4eck.springkod.entity.datasitory_file.ImageUrl;
import com.arnor4eck.springkod.repository.ImageUrlRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class CloudinaryService {

    private final ImageUrlRepository imageUrlsRepository;

    private final ImageUrlSaver imageUrlSaver;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void asyncUploadToCloudinary(DatasitoryFile file, byte[] data) throws Exception {
        String url = imageUrlSaver.upload(data);

        imageUrlsRepository.save(ImageUrl.builder()
                .datasitoryFile(file)
                .url(url)
                .build());
        log.info("Фото {} успешно загружено на Cloudinary", file.getFileId());
    }
}
