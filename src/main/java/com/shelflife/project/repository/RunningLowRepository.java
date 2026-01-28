package com.shelflife.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shelflife.project.dto.runninglow.RunningLowNotification;
import com.shelflife.project.model.RunningLowSetting;

import java.util.List;
import java.util.Optional;

@Repository
public interface RunningLowRepository extends JpaRepository<RunningLowSetting, Long> {
    List<RunningLowSetting> findByStorageId(long storageId);

    List<RunningLowSetting> findByProductId(long productId);

    Optional<RunningLowSetting> findByProductIdAndStorageId(long productId, long storageId);

    boolean existsByProductIdAndStorageId(long productId, long storageId);

    @Query(value = """
            SELECT new com.shelflife.project.dto.runninglow.RunningLowNotification(si.storage, si.product, rl.runningLow, COUNT(si))
            FROM StorageItem si
            JOIN RunningLowSetting rl ON si.storage = rl.storage AND si.product = rl.product
            WHERE si.storage.id = :storageId
            GROUP BY si.storage, si.product
            HAVING COUNT(si) <= rl.runningLow""")
    List<RunningLowNotification> findItemsRunningLow(long storageId);
}
