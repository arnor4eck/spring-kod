package com.arnor4eck.springkod.service;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;
import com.arnor4eck.springkod.entity.favorite.Favourites;
import com.arnor4eck.springkod.entity.user.User;
import com.arnor4eck.springkod.repository.FavoriteRepository;
import com.arnor4eck.springkod.util.exception.BusinessException;
import com.arnor4eck.springkod.util.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    final FavoriteRepository favoriteRepository;
    final UserService userService;
    final DatasitoryService datasitoryService;

    public void addFavorite(String email, Long id){
        User user = userService.getUser(email);
        Datasitory datasitory = datasitoryService.getById(id);
        try {
            Favourites favourites = new Favourites(user, datasitory);
            favoriteRepository.save(favourites);
        }
        catch (Exception ex){
            throw new BusinessException("Filed to create favorite");
        }
    }

    public List<Long> getFavoritesIds(String email){
        User user = userService.getUser(email);
        List<Long> datasitoryIds = favoriteRepository.findDatasitoryIdsByUser(user);
        return datasitoryIds;
    }

    public void deleteFavorite(String email, Long id){
        User user = userService.getUser(email);
        Datasitory datasitory = datasitoryService.getById(id);
        try {
            Favourites favourites = favoriteRepository.findByUserAndDatasitory(user, datasitory)
                    .orElseThrow(()-> new NotFoundException("Favorite not found"));
            favoriteRepository.delete(favourites);
        }
        catch (Exception ex){
            throw new BusinessException("Filed to delete favorite");
        }
    }
}
