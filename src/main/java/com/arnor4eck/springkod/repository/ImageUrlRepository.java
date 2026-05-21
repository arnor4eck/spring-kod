package com.arnor4eck.springkod.repository;

import com.arnor4eck.springkod.entity.datasitory_file.ImageUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImageUrlRepository extends JpaRepository<ImageUrl, Long> {
    @Query(value = "SELECT IU.* FROM image_url AS IU " +
            "JOIN DATASITORY_FILES AS df ON df.id = iu.datasitory_file_id " +
            "WHERE df.datasitory_id = :datasitory_id", nativeQuery = true)
    List<ImageUrl> findAllByDatasitoryId(@Param("datasitory_id") long datasitoryId);
}
