package com.notebridge.project.repository;

import com.notebridge.project.model.FileMetaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMetaDataRepository extends JpaRepository<FileMetaData, Integer> {
    FileMetaData findByUniqueId(String uniqueId);
}
