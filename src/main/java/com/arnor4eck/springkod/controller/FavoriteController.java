package com.arnor4eck.springkod.controller;

import com.arnor4eck.springkod.service.DatasitoryService;
import com.arnor4eck.springkod.service.FavoriteService;
import com.arnor4eck.springkod.util.dto.datasitory.DatasitoryDto;
import com.arnor4eck.springkod.util.dto.favorites.FavoriteListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final DatasitoryService datasitoryService;

    @PostMapping("/{id}")
    public ResponseEntity<Void> addFavorite(@AuthenticationPrincipal String email,
            @PathVariable("id") long datasitoryId) {
        favoriteService.addFavorite(email, datasitoryId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/my")
    public ResponseEntity<FavoriteListResponseDto> getFavoriteDatasitories(
            @AuthenticationPrincipal String email){
        var datasitoriesIds = favoriteService.getFavoritesIds(email);
        var response = new FavoriteListResponseDto(
                datasitoryService.getDatasitoriesByIds(datasitoriesIds)
                        .stream().map(DatasitoryDto::new).toList()
                );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFavoriteByDatasitoryId(@AuthenticationPrincipal String email,
            @PathVariable("id") long datasitoryId){
      favoriteService.deleteFavorite(email, datasitoryId);
      return ResponseEntity.ok().build();
    }
}
