package com.shelflife.project.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shelflife.project.exception.MemberException;
import com.shelflife.project.exception.ItemNotFoundException;
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

    public List<Storage> getStorages() {
        return storageRepository.findAll();
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

    public List<StorageItem> getProductsInStorage(final long storageId) throws ItemNotFoundException {
        return storageItemRepository.findByStorageId(storageId);
    }

    public List<StorageItem> getExpiredProductsInStorage(final long storageId) throws ItemNotFoundException {
        List<StorageItem> items = storageItemRepository.findByStorageId(storageId);

        LocalDateTime now = LocalDateTime.now();
        List<StorageItem> expired = items.stream()
                .filter(p -> p.getCreatedAt()
                        .plusDays(p.getProduct().getExpirationDaysDelta())
                        .isBefore(now))
                .collect(Collectors.toList());

        return expired;
    }

    public List<StorageItem> getProductsAboutToExpire(final long storageId) throws ItemNotFoundException {
        List<StorageItem> items = storageItemRepository.findByStorageId(storageId);

        LocalDateTime now = LocalDateTime.now();
        List<StorageItem> aboutToExpire = items.stream()
                .filter(p -> p.getCreatedAt()
                        .plusDays(p.getProduct().getExpirationDaysDelta() - 1)
                        .isBefore(now))
                .collect(Collectors.toList());

        return aboutToExpire;
    }

    public boolean isMemberOrOwnerOfStorage(final long storageId, final long userId) {
        return storageMemberRepository.existsByStorageIdAndUserId(storageId, userId);
    }

    @Transactional
    public void addMemberToStorage(final long storageId, final long userId)
            throws ItemNotFoundException, MemberException {
        if (isMemberOrOwnerOfStorage(storageId, userId))
            throw new MemberException(true);

        Storage storage = getStorage(storageId);
        User user = userService.getUserById(userId);

        StorageMember member = new StorageMember();
        member.setUser(user);
        member.setStorage(storage);

        storageMemberRepository.save(member);
    }

    @Transactional
    public void removeMemberFromStorage(final long storageId, final long userId)
            throws ItemNotFoundException, MemberException {
        if (!isMemberOrOwnerOfStorage(storageId, userId))
            throw new MemberException(false);

        Optional<StorageMember> member = storageMemberRepository.findByStorageIdAndUserId(storageId, userId);

        if (!member.isPresent())
            throw new ItemNotFoundException();

        storageMemberRepository.deleteById(member.get().getId());
    }
}
