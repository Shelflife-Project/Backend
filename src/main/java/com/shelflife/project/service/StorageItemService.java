package com.shelflife.project.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.shelflife.project.dto.storage.AddItemRequest;
import com.shelflife.project.dto.storage.EditItemRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.StorageItem;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageItemRepository;
import com.shelflife.project.repository.StorageRepository;

import jakarta.transaction.Transactional;

@Service
public class StorageItemService {
    @Autowired
    private StorageItemRepository storageItemRepository;

    @Autowired
    private StorageAccessService storageAccessService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StorageRepository storageRepository;

    public Storage getStorage(final long id) throws ItemNotFoundException {
        Optional<Storage> storage = storageRepository.findById(id);

        if (!storage.isPresent())
            throw new ItemNotFoundException("id", "Storage with this id was not found");

        return storage.get();
    }

    public List<StorageItem> getItemsInStorage(final long storageId) throws ItemNotFoundException {
        if (!storageRepository.existsById(storageId))
            throw new ItemNotFoundException("id", "Storage with this id was not found");

        return storageItemRepository.findByStorageId(storageId);
    }

    public List<StorageItem> getItemsInStorage(final long storageId, User current)
            throws ItemNotFoundException, AccessDeniedException {
        if (!storageAccessService.canAccessStorage(storageId, current))
            throw new AccessDeniedException("You can't access this storage");

        return getItemsInStorage(storageId);
    }

    @Transactional
    public StorageItem addItemToStorage(final long storageId, AddItemRequest request, User current)
            throws AccessDeniedException, ItemNotFoundException, IllegalArgumentException {

        if (!storageAccessService.canAccessStorage(storageId, current))
            throw new AccessDeniedException("You can't access this storage");

        if (request.getExpiresAt().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("expiresAt");

        Storage storage = getStorage(storageId);
        Product product = productService.getProductByID(request.getProductId());

        StorageItem item = new StorageItem();
        item.setProduct(product);
        item.setStorage(storage);
        item.setExpiresAt(request.getExpiresAt());

        return storageItemRepository.save(item);
    }

    @Transactional
    public StorageItem editItem(final long storageItemId, EditItemRequest request, User current)
            throws AccessDeniedException, ItemNotFoundException, IllegalArgumentException {
        Optional<StorageItem> item = storageItemRepository.findById(storageItemId);

        if (!item.isPresent())
            throw new ItemNotFoundException("id", "Storage item with this id was not found");

        if (!storageAccessService.canAccessStorage(item.get().getStorage().getId(), current))
            throw new AccessDeniedException("You can't access this storage");

        if (request.getExpiresAt().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("expiresAt");

        item.get().setExpiresAt(request.getExpiresAt());
        return storageItemRepository.save(item.get());
    }

    @Transactional
    public void removeItemFromStorage(final long storageItemId, User current)
            throws AccessDeniedException, ItemNotFoundException {
        Optional<StorageItem> item = storageItemRepository.findById(storageItemId);

        if (!item.isPresent())
            throw new ItemNotFoundException("id", "Storage item with this id was not found");

        if (!storageAccessService.canAccessStorage(item.get().getStorage().getId(), current))
            throw new AccessDeniedException("You can't access this storage");

        storageItemRepository.deleteById(storageItemId);
    }
}
