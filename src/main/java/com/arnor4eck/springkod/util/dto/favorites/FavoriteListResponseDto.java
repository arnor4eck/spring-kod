package com.arnor4eck.springkod.util.dto.favorites;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;
import com.arnor4eck.springkod.util.dto.datasitory.DatasitoryDto;

import java.util.List;

public record FavoriteListResponseDto(
        List<DatasitoryDto> datasitoryDtoList
) {
}
