package com.arnor4eck.springkod.util.exception;

public class DownloadFileException extends RuntimeException{
    public DownloadFileException(String massage){
        super(massage);
    }
}
