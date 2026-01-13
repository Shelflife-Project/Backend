package com.shelflife.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shelflife.project.model.StorageMember;

import java.util.List;
import java.util.Optional;

@Repository
public interface StorageMemberRepository extends JpaRepository<StorageMember, Long> {
    List<StorageMember> findByStorageId(long storageId);

    @Query("SELECT s FROM StorageMember s WHERE s.storage.id = :storageId AND s.isAccepted = true")
    List<StorageMember> findAcceptedByStorageId(long storageId);

    @Query("SELECT s FROM StorageMember s WHERE s.user.id = :userId AND s.isAccepted = true")
    List<StorageMember> findAcceptedByUserId(long userId);

    @Query("SELECT s FROM StorageMember s WHERE s.user.id = :userId AND s.isAccepted = false")
    List<StorageMember> findInvitesByUserId(long userId);

    Optional<StorageMember> findByStorageIdAndUserId(long storageId, long userId);

    boolean existsByStorageIdAndUserId(long storageId, long userId);

    @Query("""
        SELECT CASE
        WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM StorageMember s
        WHERE s.storage.id = :storageId
        AND s.user.id = :userId
        AND s.isAccepted = true
    """)
    boolean isMember(long storageId, long userId);
}