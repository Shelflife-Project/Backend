package com.shelflife.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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

    @Transactional
    public Storage createStorage(CreateStorageRequest request, User current)
            throws AccessDeniedException, IllegalArgumentException {

        if (current == null)
            throw new AccessDeniedException(null);

        if (request.getName() == null || request.getName().isBlank())
            throw new IllegalArgumentException("name");

        Storage storage = new Storage();
        storage.setOwner(current);
        storage.setName(request.getName());

        return storageRepository.save(storage);
    }

    @Transactional
    public Storage changeName(final long id, ChangeStorageNameRequest request, User current)
            throws AccessDeniedException, ItemNotFoundException {

        if (current == null)
            throw new AccessDeniedException(null);

        if (request.getName() == null || request.getName().isBlank())
            throw new IllegalArgumentException("name");

        Storage storage = storageGetterService.getStorage(id);

        if (!current.isAdmin() && storage.getOwner().getId() != current.getId())
            throw new AccessDeniedException("You can't rename this storage");

        storage.setName(request.getName());
        return storageRepository.save(storage);
    }

    @Transactional
    public void deleteStorageRequest(final long id, User current)
            throws AccessDeniedException, ItemNotFoundException {
        if (current == null)
            throw new AccessDeniedException(null);

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
