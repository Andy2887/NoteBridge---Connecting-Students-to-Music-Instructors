package com.notebridge.project.repository;

import com.notebridge.project.model.FileMetaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FileMetaDataRepository extends JpaRepository<FileMetaData, Integer> {
    FileMetaData findByUniqueId(String uniqueId);
    @Transactional
    int deleteByUniqueId(String uniqueId);
}
