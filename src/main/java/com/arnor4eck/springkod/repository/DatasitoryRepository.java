package com.arnor4eck.springkod.repository;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DatasitoryRepository extends JpaRepository<Datasitory, Long> {
    @Query(value = "SELECT DISTINCT d.* FROM datasitory d " +
            "LEFT JOIN datasitory_members dm ON d.id = dm.datasitory_id " +
            "LEFT JOIN users u ON u.id = d.creator_id OR u.id = dm.user_id " +
            "WHERE u.email = :email",
            nativeQuery = true)
    List<Datasitory> findAllByUserEmail(@Param("email") String email);
}
