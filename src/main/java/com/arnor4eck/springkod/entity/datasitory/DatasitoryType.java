package com.arnor4eck.springkod.entity.datasitory;

public enum DatasitoryType {
    OPEN, PRIVATE;

    public static String getName(DatasitoryType type){
        return switch (type){
            case OPEN -> "Публичный";
            case PRIVATE -> "Приватный";
        };
    }
}
