package com.shelflife.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shelflife.project.model.StorageMember;

import java.util.List;

@Repository
public interface StorageMemberRepository extends JpaRepository<StorageMember, Long> {
    List<StorageMember> findByStorageId(long storageId);
    List<StorageMember> findByUserId(long userId);
}