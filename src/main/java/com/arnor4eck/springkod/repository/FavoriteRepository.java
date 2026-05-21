package com.arnor4eck.springkod.repository;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;
import com.arnor4eck.springkod.entity.favorite.Favourites;
import com.arnor4eck.springkod.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favourites, Long> {
    @Query("SELECT f.datasitory.id FROM Favourites f WHERE f.user = :user")
    List<Long> findDatasitoryIdsByUser(@Param("user") User user);

    Optional<Favourites> findByUserAndDatasitory(User user, Datasitory datasitory);
}
