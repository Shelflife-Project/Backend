package com.shelflife.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shelflife.project.model.ShoppingListItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {
    List<ShoppingListItem> findByStorageId(long storageId);

    List<ShoppingListItem> findByProductId(long productId);

    Optional<ShoppingListItem> findByProductIdAndStorageId(long productId, long storageId);

    boolean existsByProductIdAndStorageId(long productId, long storageId);

    List<ShoppingListItem> findByStorageIdIn(List<Long> storageIds);
}
