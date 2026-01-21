package com.shelflife.project.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.shelflife.project.dto.runninglow.CreateSettingRequest;
import com.shelflife.project.dto.runninglow.EditSettingRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.RunningLowSetting;
import com.shelflife.project.model.Storage;
import com.shelflife.project.repository.RunningLowRepository;

import jakarta.transaction.Transactional;

@Service
public class RunningLowService {
    @Autowired
    private RunningLowRepository repository;

    @Autowired
    private StorageMemberService storageMemberService;

    @Autowired
    private ProductService productService;

    RunningLowSetting getSetting(final long settingId) throws ItemNotFoundException {
        Optional<RunningLowSetting> setting = repository.findById(settingId);

        if (!setting.isPresent())
            throw new ItemNotFoundException("settingId", "Setting with this id was not found");

        return setting.get();
    }

    List<RunningLowSetting> getSettingsForStorage(final long storageId, Authentication auth)
            throws AccessDeniedException {
        if (!storageMemberService.canAccessStorage(storageId, auth))
            throw new AccessDeniedException("You can't access this storage");

        return repository.findByStorageId(storageId);
    }

    List<Product> getRunningLowInStorage(final long storageId, Authentication auth)
            throws AccessDeniedException {
        if (!storageMemberService.canAccessStorage(storageId, auth))
            throw new AccessDeniedException("You can't access this storage");

        return repository.findItemsRunningLow(storageId);
    }

    @Transactional
    RunningLowSetting createSetting(final long storageId, CreateSettingRequest request, Authentication auth)
            throws AccessDeniedException, ItemNotFoundException {
        Storage storage = storageMemberService.getStorage(storageId);
        Product product = productService.getProductByID(request.getProductId());

        if (!storageMemberService.canAccessStorage(storageId, auth))
            throw new AccessDeniedException("You can't access this storage");

        RunningLowSetting setting = new RunningLowSetting();
        setting.setStorage(storage);
        setting.setProduct(product);

        return repository.save(setting);
    }

    @Transactional
    RunningLowSetting editSetting(final long settingId, EditSettingRequest request, Authentication auth)
            throws AccessDeniedException, IllegalArgumentException {
        RunningLowSetting setting = getSetting(settingId);

        if (!storageMemberService.canAccessStorage(setting.getStorage().getId(), auth))
            throw new AccessDeniedException("You can't access this storage");

        if (request.getRunningLow() < 0)
            throw new IllegalArgumentException("runningLow");

        setting.setRunningLow(request.getRunningLow());
        return repository.save(setting);
    }

    @Transactional
    void deleteSetting(final long settingId, Authentication auth)
            throws ItemNotFoundException, AccessDeniedException {
        RunningLowSetting setting = getSetting(settingId);

        if (!storageMemberService.canAccessStorage(setting.getStorage().getId(), auth))
            throw new AccessDeniedException("You can't access this storage");

        repository.delete(setting);
    }
}
