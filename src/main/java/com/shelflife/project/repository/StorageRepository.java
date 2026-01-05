package com.shelflife.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shelflife.project.model.Storage;
import java.util.List;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Long> {
    List<Storage> findByOwnerId(long ownerId);

    @Query("""
            SELECT distinct s 
            FROM Storage s
            LEFT JOIN s.members sm
            WHERE s.owner.id = :userId OR sm.user.id = :userId
    """)
    List<Storage> findAccessibleStorages(long userId);
}