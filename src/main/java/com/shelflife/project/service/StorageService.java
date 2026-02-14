package com.shelflife.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.shelflife.project.dto.storage.ChangeStorageNameRequest;
import com.shelflife.project.dto.storage.CreateStorageRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageRepository;

import jakarta.transaction.Transactional;

@Service
public class StorageService {

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private StorageGetterService storageGetterService;

    @Autowired
    private StorageMemberService storageMemberService;

    @Autowired
    private UserService userService;

    @Transactional
    public Storage createStorage(CreateStorageRequest request, Authentication auth)
            throws AccessDeniedException, IllegalArgumentException {

        if (request.getName() == null || request.getName().isBlank())
            throw new IllegalArgumentException("name");

        User current = userService.getUserByAuth(auth);
        Storage storage = new Storage();
        storage.setOwner(current);
        storage.setName(request.getName());

        return storageRepository.save(storage);
    }

    @Transactional
    public Storage changeName(final long id, ChangeStorageNameRequest request, Authentication auth)
            throws AccessDeniedException, ItemNotFoundException {

        if (request.getName() == null || request.getName().isBlank())
            throw new IllegalArgumentException("name");

        User current = userService.getUserByAuth(auth);
        Storage storage = storageGetterService.getStorage(id);

        if (!current.isAdmin() && storage.getOwner().getId() != current.getId())
            throw new AccessDeniedException("You can't rename this storage");

        storage.setName(request.getName());
        return storageRepository.save(storage);
    }

    @Transactional
    public void deleteStorageRequest(final long id, Authentication auth)
            throws AccessDeniedException, ItemNotFoundException {
        User current = userService.getUserByAuth(auth);
        Storage storage = storageGetterService.getStorage(id);

        if (current.isAdmin() || storage.getOwner().getId() == current.getId()) { // Delete storage
            storageRepository.deleteById(id);
        } else if (storageMemberService.isMemberOfStorage(id, current.getId())) { // Leave storage
            storageMemberService.removeMemberFromStorage(id, current.getId());
        } else {
            throw new AccessDeniedException("You can't delete this storage");
        }
    }
}
