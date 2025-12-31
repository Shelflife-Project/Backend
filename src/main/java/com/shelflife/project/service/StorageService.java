package com.shelflife.project.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    public Storage getStorage(final long id) throws ItemNotFoundException {
        Optional<Storage> storage = storageRepository.findById(id);

        if (!storage.isPresent())
            throw new ItemNotFoundException();

        return storage.get();
    }

    public Storage getStorage(Authentication auth, final long storageId)
            throws AccessDeniedException, ItemNotFoundException {

        if (!canAccessStorage(storageId, auth))
            throw new AccessDeniedException(null);

        return getStorage(storageId);
    }

    public List<StorageItem> getItemsInStorage(final long storageId) throws ItemNotFoundException {
        if (!storageRepository.existsById(storageId))
            throw new ItemNotFoundException();

        return storageItemRepository.findByStorageId(storageId);
    }

    public List<StorageItem> getItemsInStorage(final long storageId, Authentication auth)
            throws ItemNotFoundException, AccessDeniedException {
        if (!canAccessStorage(storageId, auth))
            throw new AccessDeniedException(null);

        return getItemsInStorage(storageId);
    }

    public List<StorageItem> getExpiredItemsInStorage(final long storageId) throws ItemNotFoundException {
        return storageItemRepository.findExpired(storageId);
    }

    public List<StorageItem> getExpiredItemsInStorage(final long storageId, Authentication auth)
            throws ItemNotFoundException, AccessDeniedException {
        if (!canAccessStorage(storageId, auth))
            throw new AccessDeniedException(null);

        return getExpiredItemsInStorage(storageId);
    }

    public List<StorageItem> getItemsAboutToExpire(final long storageId) throws ItemNotFoundException {
        LocalDateTime date = LocalDateTime.now().plusDays(1);
        return storageItemRepository.findByExpiresAtBefore(storageId, date);
    }

    public List<StorageItem> getItemsAboutToExpire(final long storageId, Authentication auth)
            throws ItemNotFoundException, AccessDeniedException {

        if (!canAccessStorage(storageId, auth))
            throw new AccessDeniedException(null);

        return getItemsAboutToExpire(storageId);
    }

    public boolean canAccessStorage(final long storageId, final long userId) {
        try {
            User user = userService.getUserById(userId);
            Storage storage = getStorage(storageId);

            if (user.isAdmin())
                return true;

            if (storage.getOwner().getId() == userId)
                return true;

            return storageMemberRepository.existsByStorageIdAndUserId(storageId, userId);
        } catch (ItemNotFoundException e) {
            return false;
        }
    }

    public boolean canAccessStorage(final long storageId, Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            Storage storage = getStorage(storageId);

            if (user.isAdmin())
                return true;

            if (storage.getOwner().getId() == user.getId())
                return true;

            return storageMemberRepository.existsByStorageIdAndUserId(storageId, user.getId());
        } catch (ItemNotFoundException e) {
            return false;
        } catch (AccessDeniedException e) {
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
        item.setExpiresAt(LocalDateTime.now().plusDays(product.getExpirationDaysDelta()));

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
