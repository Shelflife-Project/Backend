package com.shelflife.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.shelflife.project.model.RunningLowSetting;

import java.util.List;

@Repository
public interface RunningLowRepository extends JpaRepository<RunningLowSetting, Long> {
    List<RunningLowSetting> findByStorageId(long storageId);

    List<RunningLowSetting> findByProductId(long productId);
}
