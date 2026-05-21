package com.arnor4eck.springkod.util.file.url;

import com.cloudinary.Cloudinary;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;

@Component
@AllArgsConstructor
public class CloudinaryFileUploader implements ImageUrlSaver {

    private final Cloudinary cloudinary;

    @Override
    public String upload(byte[] data) throws IOException {
        return cloudinary.uploader().upload(data, new HashMap<>())
                .get("url").toString();
    }
}