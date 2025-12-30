package com.shelflife.project.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.shelflife.project.exception.MemberException;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.StorageItem;
import com.shelflife.project.model.StorageMember;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageItemRepository;
import com.shelflife.project.repository.StorageMemberRepository;
import com.shelflife.project.repository.StorageRepository;

import jakarta.transaction.Transactional;

@Service
public class StorageService {

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private StorageItemRepository storageItemRepository;

    @Autowired
    private StorageMemberRepository storageMemberRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    public List<Storage> getStorages() {
        return storageRepository.findAll();
    }

    public List<Storage> getStorages(Authentication auth) throws AccessDeniedException {
        User user = userService.getUserByAuth(auth);

        if (user.isAdmin())
            return getStorages();

        return storageRepository.findAccessibleStorages(user.getId());
    }

    public List<Storage> getAccessibleStorages(final long userId) {
        return storageRepository.findAccessibleStorages(userId);
    }

    public Storage getStorage(final long id) throws ItemNotFoundException {
        Optional<Storage> storage = storageRepository.findById(id);

        if (!storage.isPresent())
            throw new ItemNotFoundException();

        return storage.get();
    }

    public Storage getStorage(Authentication auth, final long storageId)
            throws AccessDeniedException, ItemNotFoundException {
        Storage storage = getStorage(storageId);
        User user = userService.getUserByAuth(auth);

        if (!canAccessStorage(storageId, user.getId()))
            throw new AccessDeniedException(null);

        return storage;
    }

    public List<StorageItem> getItemsInStorage(final long storageId) throws ItemNotFoundException {
        return storageItemRepository.findByStorageId(storageId);
    }

    public List<StorageItem> getItemsInStorage(final long storageId, Authentication auth)
            throws ItemNotFoundException, AccessDeniedException {
        User user = userService.getUserByAuth(auth);

        if (!canAccessStorage(storageId, user.getId()))
            throw new AccessDeniedException(null);

        return getItemsInStorage(storageId);
    }

    public List<StorageItem> getExpiredItemsInStorage(final long storageId) throws ItemNotFoundException {
        List<StorageItem> items = storageItemRepository.findByStorageId(storageId);

        LocalDateTime now = LocalDateTime.now();
        List<StorageItem> expired = items.stream()
                .filter(p -> p.getCreatedAt()
                        .plusDays(p.getProduct().getExpirationDaysDelta())
                        .isBefore(now))
                .collect(Collectors.toList());

        return expired;
    }

    public List<StorageItem> getExpiredItemsInStorage(final long storageId, Authentication auth)
            throws ItemNotFoundException, AccessDeniedException {
        User current = userService.getUserByAuth(auth);

        if (!canAccessStorage(storageId, current.getId()))
            throw new AccessDeniedException(null);

        return getExpiredItemsInStorage(storageId);
    }

    public List<StorageItem> getItemsAboutToExpire(final long storageId) throws ItemNotFoundException {
        List<StorageItem> items = storageItemRepository.findByStorageId(storageId);

        LocalDateTime now = LocalDateTime.now();
        List<StorageItem> aboutToExpire = items.stream()
                .filter(p -> p.getCreatedAt()
                        .plusDays(p.getProduct().getExpirationDaysDelta() - 1)
                        .isBefore(now))
                .collect(Collectors.toList());

        return aboutToExpire;
    }

    public boolean canAccessStorage(final long storageId, final long userId) {
        try {
            Storage storage = getStorage(storageId);
            User user = userService.getUserById(userId);

            if (user.isAdmin())
                return true;

            if (storage.getOwner().getId() == userId)
                return true;

            return storageMemberRepository.existsByStorageIdAndUserId(storageId, userId);
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Transactional
    public StorageMember addMemberToStorage(final long storageId, final long userId, Authentication auth)
            throws ItemNotFoundException, MemberException {

        Storage storage = getStorage(storageId);
        User target = userService.getUserById(userId);
        User current = userService.getUserByAuth(auth);

        if (!current.isAdmin() && storage.getOwner().getId() != current.getId())
            throw new AccessDeniedException(null);

        StorageMember member = new StorageMember();
        member.setUser(target);
        member.setStorage(storage);

        return storageMemberRepository.save(member);
    }

    public List<StorageMember> getStorageMembers(final long storageId, Authentication auth)
            throws ItemNotFoundException, AccessDeniedException {
        User user = userService.getUserByAuth(auth);

        if (!canAccessStorage(storageId, user.getId()))
            throw new AccessDeniedException(null);

        return storageMemberRepository.findByStorageId(storageId);
    }

    @Transactional
    public void removeMemberFromStorage(final long storageId, final long userId, Authentication auth)
            throws ItemNotFoundException, MemberException {

        Storage storage = getStorage(storageId);
        User current = userService.getUserByAuth(auth);

        if (!current.isAdmin() && storage.getOwner().getId() != current.getId())
            throw new AccessDeniedException(null);

        Optional<StorageMember> member = storageMemberRepository.findByStorageIdAndUserId(storageId, userId);

        if (!member.isPresent())
            throw new ItemNotFoundException();

        storageMemberRepository.deleteById(member.get().getId());
    }

    @Transactional
    public StorageItem addItemToStorage(final long storageId, final long productId, Authentication auth)
            throws AccessDeniedException, ItemNotFoundException {

        User current = userService.getUserByAuth(auth);

        if (!canAccessStorage(storageId, current.getId()))
            throw new AccessDeniedException(null);

        Storage storage = getStorage(storageId);
        Product product = productService.getProductByID(productId);

        StorageItem item = new StorageItem();
        item.setProduct(product);
        item.setStorage(storage);

        return storageItemRepository.save(item);
    }

    @Transactional
    public void removeItemFromStorage(final long storageItemId, Authentication auth)
            throws AccessDeniedException, ItemNotFoundException {
        User current = userService.getUserByAuth(auth);

        Optional<StorageItem> item = storageItemRepository.findById(storageItemId);

        if (!item.isPresent())
            throw new ItemNotFoundException();

        if (!canAccessStorage(item.get().getStorage().getId(), current.getId()))
            throw new AccessDeniedException(null);

        storageItemRepository.deleteById(storageItemId);
    }
}
