package com.shelflife.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageRepository;

@Service
public class StorageAccessService {
    @Autowired
    private StorageRepository storageRepository;

    /**
     * @return True if you are an ADMIN, or an OWNER, or an accepted MEMBER
     */
    public boolean canAccessStorage(long storageId, User user) {
        if(user == null)
            return false;

        if(user.isAdmin())
            return true;

        return storageRepository.isMemberOrOwner(storageId, user.getId());
    }
}
