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
            SELECT new com.shelflife.project.dto.runninglow.RunningLowNotification(rl.storage, rl.product, rl.runningLow, COUNT(si.product.id))
            FROM RunningLowSetting rl
            LEFT JOIN StorageItem si ON si.storage = rl.storage AND si.product = rl.product
            WHERE rl.storage.id = :storageId
            GROUP BY rl.product.id, rl.storage.id
            HAVING COUNT(si.product.id) <= rl.runningLow""")
    List<RunningLowNotification> findItemsRunningLow(long storageId);
}
