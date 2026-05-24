package com.arnor4eck.springkod.util.exception;

public class FileNotFoundInStorageException extends NotFoundException {
    public FileNotFoundInStorageException(String message) {
        super(message);
    }
}
