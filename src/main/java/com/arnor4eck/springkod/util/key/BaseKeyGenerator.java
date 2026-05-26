package com.arnor4eck.springkod.util.key;

import org.springframework.stereotype.Component;

@Component
public class BaseKeyGenerator implements KeyGenerator{
    @Override
    public String generateKey(String filename, long datasitoryId) {
        return String.format("%d-%s", datasitoryId, filename);
    }
}
