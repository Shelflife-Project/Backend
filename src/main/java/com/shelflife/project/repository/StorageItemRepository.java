package com.shelflife.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shelflife.project.model.StorageItem;

import java.util.List;
import java.time.LocalDateTime;


@Repository
public interface StorageItemRepository extends JpaRepository<StorageItem, Long> {
    List<StorageItem> findByStorageId(long storageId);

    @Query("SELECT s FROM StorageItem s WHERE s.storage.id = :storageId AND s.expiresAt < CURRENT_DATE")
    List<StorageItem> findExpired(long storageId);

    @Query("SELECT s FROM StorageItem s WHERE s.storage.id = :storageId AND s.expiresAt < :time")
    List<StorageItem> findByExpiresAtBefore(long storageId, LocalDateTime time);
}