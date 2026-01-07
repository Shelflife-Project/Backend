package com.shelflife.project.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.shelflife.project.dto.ChangeStorageNameRequest;
import com.shelflife.project.dto.CreateStorageRequest;
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
            throws AccessDeniedException, IllegalArgumentException, ItemNotFoundException {
        if (request.getName() == null || request.getName().isBlank())
            throw new IllegalArgumentException("name");

        User current = userService.getUserByAuth(auth);
        Storage storage = getStorage(id);

        if (storage.getOwner().getId() != current.getId() && !current.isAdmin())
            throw new AccessDeniedException(null);

        storage.setName(request.getName());
        return storageRepository.save(storage);
    }

    @Transactional
    public void deleteStorageRequest(final long id, Authentication auth)
            throws AccessDeniedException, ItemNotFoundException {
        User current = userService.getUserByAuth(auth);
        Storage storage = getStorage(id);

        if (current.isAdmin() || storage.getOwner().getId() == current.getId()) { // Delete storage
            storageRepository.deleteById(id);
        } else if (storageMemberService.isMemberOfStorage(id, current.getId())) { // Leave storage
            storageMemberService.removeMemberFromStorage(id, current.getId());
        } else {
            throw new AccessDeniedException(null);
        }
    }

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
            throw new ItemNotFoundException("id", "Storage with this id was not found");

        return storage.get();
    }

    public Storage getStorage(Authentication auth, final long storageId)
            throws AccessDeniedException, ItemNotFoundException {

        if (!storageMemberService.canAccessStorage(storageId, auth)) {
            if (!storageRepository.existsById(storageId))
                throw new ItemNotFoundException("id", "Storage with this id was not found");

            throw new AccessDeniedException(null);
        }

        return getStorage(storageId);
    }
}
