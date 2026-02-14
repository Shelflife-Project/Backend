package com.shelflife.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageRepository;

@Service
public class StorageAccessService {
    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private UserService userService;

    public boolean canAccessStorage(long storageId, long userId) {
        if (userService.isAdmin(userId))
            return true;

        if (storageRepository.isMemberOrOwner(storageId, userId))
            return true;

        return false;
    }

    public boolean canAccessStorage(long storageId, Authentication auth) {

        try {
            User user = userService.getUserByAuth(auth);
            return canAccessStorage(storageId, user.getId());
        } catch (AccessDeniedException e) {
        }

        return false;
    }
}
