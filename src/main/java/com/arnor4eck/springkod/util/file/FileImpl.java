package com.arnor4eck.springkod.util.file;

import com.arnor4eck.springkod.entity.datasitory_file.FileType;
import org.jspecify.annotations.Nullable;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Arrays;

public final class FileImpl implements MultipartFile {

    private final String name;
    private final String originalName;
    private final String contentType;
    private FileType fileType;
    private byte[] content;

    public FileImpl(String name, String originalName,
                    String contentType, FileType fileType, byte[] content) {
        this.name = name;
        this.originalName = originalName;
        this.contentType = contentType;
        this.content = Arrays.copyOf(content, content.length);
        this.fileType = fileType;
    }

    public FileImpl(String name, String originalName,
                    MimeType type, FileType fileType, byte[] content) {
        this(name, originalName, type.getType(), fileType, content);
    }

    public FileImpl(MultipartFile file, FileType fileType) throws IOException {
        this(file.getName(), file.getOriginalFilename(),
            file.getContentType(), fileType,
            file.getBytes());
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @Nullable String getOriginalFilename() {
        return originalName;
    }

    @Override
    public @Nullable String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return Arrays.copyOf(content, content.length);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        try(OutputStream out = new FileOutputStream(dest)) {
            out.write(content);
        }
    }
}
