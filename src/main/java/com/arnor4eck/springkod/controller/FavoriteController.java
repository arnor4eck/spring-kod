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
@RequestMapping("/favorite")
public class FavoriteController {
    final FavoriteService favoriteService;
    final DatasitoryService datasitoryService;
    @PostMapping("/{id}")
    public ResponseEntity<Void> addFavorite(@AuthenticationPrincipal String email,
            @PathVariable long id) {
        favoriteService.addFavorite(email, id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/")
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
            @PathVariable long id){
      favoriteService.deleteFavorite(email, id);
      return ResponseEntity.ok().build();
    }
}
