package com.shelflife.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shelflife.project.model.StorageMember;

import java.util.List;
import java.util.Optional;

@Repository
public interface StorageMemberRepository extends JpaRepository<StorageMember, Long> {
    List<StorageMember> findByStorageId(long storageId);

    List<StorageMember> findByUserId(long userId);

    Optional<StorageMember> findByStorageIdAndUserId(long storageId, long userId);

    boolean existsByStorageIdAndUserId(long storageId, long userId);
}