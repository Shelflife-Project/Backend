package com.shelflife.project.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageRepository;

@Service
public class StorageGetterService {
    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private UserService userService;

    public Storage getStorage(final long id) throws ItemNotFoundException {
        Optional<Storage> storage = storageRepository.findById(id);

        if (!storage.isPresent())
            throw new ItemNotFoundException("id", "Storage with this id was not found");

        return storage.get();
    }

    public Storage getStorage(Authentication auth, final long storageId)
            throws AccessDeniedException, ItemNotFoundException {

        User user = userService.getUserByAuth(auth);

        if (!user.isAdmin()) {
            if (!storageRepository.isMemberOrOwner(storageId, user.getId())) {
                throw new AccessDeniedException("You can't access this storage");
            }
        }

        return getStorage(storageId);
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
}
