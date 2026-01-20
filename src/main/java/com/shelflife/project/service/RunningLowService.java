package com.shelflife.project.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.shelflife.project.model.RunningLowSetting;
import com.shelflife.project.repository.RunningLowRepository;

@Service
public class RunningLowService {
    @Autowired
    private RunningLowRepository repository;

    @Autowired
    private StorageMemberService storageMemberService;

    List<RunningLowSetting> getSettingsForStorage(final long storageId, Authentication auth)
            throws AccessDeniedException {
        if (!storageMemberService.canAccessStorage(storageId, auth))
            throw new AccessDeniedException("You can't access this storage");

        return repository.findByStorageId(storageId);
    }
}
