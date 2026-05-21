package com.arnor4eck.springkod.util.file.url;

import java.io.IOException;

public interface ImageUrlSaver {
    String upload(byte[] data) throws IOException;
}
