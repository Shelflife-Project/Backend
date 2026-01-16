package com.shelflife.project.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
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
    private StorageMemberService storageMemberService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

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

    public List<StorageItem> getItemsInStorage(final long storageId, Authentication auth)
            throws ItemNotFoundException, AccessDeniedException {
        if (!storageMemberService.canAccessStorage(storageId, auth))
            throw new AccessDeniedException(null);

        return getItemsInStorage(storageId);
    }

    public List<StorageItem> getExpiredItemsInStorage(final long storageId) throws ItemNotFoundException {
        if (!storageRepository.existsById(storageId))
            throw new ItemNotFoundException("id", "Storage with this id was not found");

        return storageItemRepository.findExpired(storageId);
    }

    public List<StorageItem> getExpiredItemsInStorage(final long storageId, Authentication auth)
            throws ItemNotFoundException, AccessDeniedException {
        if (!storageMemberService.canAccessStorage(storageId, auth))
            throw new AccessDeniedException(null);

        return getExpiredItemsInStorage(storageId);
    }

    public List<StorageItem> getItemsAboutToExpire(final long storageId) throws ItemNotFoundException {
        LocalDate date = LocalDate.now().plusDays(1);
        return storageItemRepository.findByExpiresAtBefore(storageId, date);
    }

    public List<StorageItem> getItemsAboutToExpire(final long storageId, Authentication auth)
            throws ItemNotFoundException, AccessDeniedException {

        if (!storageMemberService.canAccessStorage(storageId, auth))
            throw new AccessDeniedException(null);

        return getItemsAboutToExpire(storageId);
    }

    @Transactional
    public StorageItem addItemToStorage(final long storageId, AddItemRequest request, Authentication auth)
            throws AccessDeniedException, ItemNotFoundException, IllegalArgumentException {

        if (!storageMemberService.canAccessStorage(storageId, auth))
            throw new AccessDeniedException(null);

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
    public StorageItem editItem(final long storageItemId, EditItemRequest request, Authentication auth)
            throws AccessDeniedException, ItemNotFoundException, IllegalArgumentException {
        User current = userService.getUserByAuth(auth);
        Optional<StorageItem> item = storageItemRepository.findById(storageItemId);

        if (!item.isPresent())
            throw new ItemNotFoundException("id", "Storage item with this id was not found");

        if (!storageMemberService.canAccessStorage(item.get().getStorage().getId(), current.getId()))
            throw new AccessDeniedException(null);

        if (request.getExpiresAt().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("expiresAt");

        item.get().setExpiresAt(request.getExpiresAt());
        return storageItemRepository.save(item.get());
    }

    @Transactional
    public void removeItemFromStorage(final long storageItemId, Authentication auth)
            throws AccessDeniedException, ItemNotFoundException {
        User current = userService.getUserByAuth(auth);

        Optional<StorageItem> item = storageItemRepository.findById(storageItemId);

        if (!item.isPresent())
            throw new ItemNotFoundException("id", "Storage item with this id was not found");

        if (!storageMemberService.canAccessStorage(item.get().getStorage().getId(), current.getId()))
            throw new AccessDeniedException(null);

        storageItemRepository.deleteById(storageItemId);
    }
}
