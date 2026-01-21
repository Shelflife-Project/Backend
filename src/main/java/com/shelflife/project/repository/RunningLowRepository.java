package com.shelflife.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shelflife.project.model.Product;
import com.shelflife.project.model.RunningLowSetting;

import java.util.List;
import java.util.Optional;

@Repository
public interface RunningLowRepository extends JpaRepository<RunningLowSetting, Long> {
    List<RunningLowSetting> findByStorageId(long storageId);

    List<RunningLowSetting> findByProductId(long productId);

    Optional<RunningLowSetting> findbyProductIdAndStorageId(long productId, long storageId);

    boolean existsByProductIdAndStorageId(long productId, long storageId);

    @Query(value = """
            SELECT p FROM StorageItem si
            JOIN RunningLowSetting rl
            ON rl.product = si.product AND rl.storage = si.storage
            JOIN si.product p
            GROUP BY si.storage.id, p.id
            HAVING COUNT(si) <= rl.runningLow""")
    List<Product> findItemsRunningLow(long storageId);
}
