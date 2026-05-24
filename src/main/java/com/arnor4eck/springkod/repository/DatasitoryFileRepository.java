package com.arnor4eck.springkod.repository;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;
import com.arnor4eck.springkod.entity.datasitory_file.DatasitoryFile;
import com.arnor4eck.springkod.entity.datasitory_file.FileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DatasitoryFileRepository extends JpaRepository<DatasitoryFile, Long> {
    List<DatasitoryFile> findAllByDatasitory(Datasitory datasitory);
    @Query(nativeQuery = true, name = "SELECT * FROM datasitory_files AS df " +
            "JOIN datasitory AS d ON d.id = df.datasitory_id " +
            "WHERE d.id = :datasitoryId AND df.fileType = :fileType")
    List<DatasitoryFile> findByDatasitoryIdAndFileType(@Param("datasitoryId") long datasitoryId,
                                                           @Param("fileType") FileType fileType);

    Optional<DatasitoryFile> findByFileId(String fileId);
}
