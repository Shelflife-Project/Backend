package com.shelflife.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shelflife.project.model.StorageItem;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StorageItemRepository extends JpaRepository<StorageItem, Long> {
    List<StorageItem> findByStorageId(long storageId);
    List<StorageItem> findByCreatedAtBefore(LocalDateTime cutoff);
}