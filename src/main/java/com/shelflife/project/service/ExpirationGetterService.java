package com.shelflife.project.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.StorageItem;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageItemRepository;
import com.shelflife.project.repository.StorageRepository;

@Service
public class ExpirationGetterService {

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private StorageGetterService storageGetterService;

    @Autowired
    private StorageAccessService storageAccessService;

    @Autowired
    private StorageItemRepository storageItemRepository;

    public List<StorageItem> getExpiredItemsInStorage(final long storageId) throws ItemNotFoundException {
        if (!storageRepository.existsById(storageId))
            throw new ItemNotFoundException("id", "Storage with this id was not found");

        return storageItemRepository.findExpired(storageId);
    }

    public List<StorageItem> getExpiredItemsInStorage(final long storageId, User current)
            throws ItemNotFoundException, AccessDeniedException {
        if (!storageAccessService.canAccessStorage(storageId, current))
            throw new AccessDeniedException("You can't access this storage");

        return getExpiredItemsInStorage(storageId);
    }

    public List<StorageItem> getItemsAboutToExpire(final long storageId) throws ItemNotFoundException {
        LocalDate date = LocalDate.now().plusDays(1);
        return storageItemRepository.findByExpiresAtBefore(storageId, date);
    }

    public List<StorageItem> getItemsAboutToExpire(final long storageId, User current)
            throws ItemNotFoundException, AccessDeniedException {

        if (!storageAccessService.canAccessStorage(storageId, current))
            throw new AccessDeniedException("You can't access this storage");

        return getItemsAboutToExpire(storageId);
    }

    public List<StorageItem> getItemsAboutToExpireAggregated(User current) throws AccessDeniedException {
        Page<Storage> storages = storageGetterService.getStorages(current, "", Pageable.unpaged());

        List<StorageItem> items = new ArrayList<>();

        for (Storage storage : storages) {
            List<StorageItem> sItems = getItemsAboutToExpire(storage.getId());
            items.addAll(sItems);
        }

        return items;
    }

    public List<StorageItem> getExpiredItemsAggregated(User current) throws AccessDeniedException {
        Page<Storage> storages = storageGetterService.getStorages(current, "", Pageable.unpaged());

        List<StorageItem> items = new ArrayList<>();

        for (Storage storage : storages) {
            List<StorageItem> sItems = getExpiredItemsInStorage(storage.getId());
            items.addAll(sItems);
        }

        return items;
    }
}
