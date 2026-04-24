package com.shelflife.project.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.shelflife.project.dto.storage.StorageSummary;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageRepository;

@Service
public class StorageGetterService {
    @Autowired
    private StorageRepository storageRepository;

    public Storage getStorage(final long id) throws ItemNotFoundException {
        Optional<Storage> storage = storageRepository.findById(id);

        if (!storage.isPresent())
            throw new ItemNotFoundException("id", "Storage with this id was not found");

        return storage.get();
    }

    public Storage getStorage(User user, final long storageId)
            throws AccessDeniedException, ItemNotFoundException {

        if (user == null)
            throw new AccessDeniedException(null);

        if (!user.isAdmin()) {
            if (!storageRepository.isMemberOrOwner(storageId, user.getId())) {
                throw new AccessDeniedException("You can't access this storage");
            }
        }

        return getStorage(storageId);
    }

    public Page<Storage> getStorages(User user, String search, Pageable pageable)
            throws AccessDeniedException {

        if (user == null)
            throw new AccessDeniedException(null);

        if (user.isAdmin())
            return storageRepository.searchAll(search, pageable);

        return storageRepository.findAccessibleStorages(user.getId(), search, pageable);
    }

    public Page<StorageSummary> getStorageSummaries(User user, String search, Pageable pageable)
            throws AccessDeniedException {

        if (user == null)
            throw new AccessDeniedException(null);

        if (user.isAdmin())
            return storageRepository.searchAllSummaries(search, pageable);

        return storageRepository.findAccessibleStorageSummaries(user.getId(), search, pageable);
    }
}
