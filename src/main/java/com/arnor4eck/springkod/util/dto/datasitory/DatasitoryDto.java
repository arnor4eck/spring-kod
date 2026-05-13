package com.arnor4eck.springkod.util.dto.datasitory;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;
import com.arnor4eck.springkod.entity.datasitory.DatasitoryType;
import com.arnor4eck.springkod.util.dto.user.UserDto;

import java.time.format.DateTimeFormatter;

public record DatasitoryDto(long id, String name,
                            String description, String type,
                            String createdAt, String updatedAt,
                            UserDto creator) {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public DatasitoryDto(Datasitory datasitory){
        this(datasitory.getId(), datasitory.getName(),
                datasitory.getDescription(), DatasitoryType.getName(datasitory.getDatasitoryType()),
                formatter.format(datasitory.getCreatedAt()),
                formatter.format(datasitory.getUpdatedAt()),
                new UserDto(datasitory.getCreator()));
    }
}
