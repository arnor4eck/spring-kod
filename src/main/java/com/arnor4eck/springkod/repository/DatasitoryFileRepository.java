package com.arnor4eck.springkod.repository;

import com.arnor4eck.springkod.entity.datasitory_file.DatasitoryFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasitoryFileRepository extends JpaRepository<DatasitoryFile, Long> {
    boolean existsByFileId(String fileId);
}
