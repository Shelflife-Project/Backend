package com.shelflife.project.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.shelflife.project.dto.runninglow.CreateSettingRequest;
import com.shelflife.project.dto.runninglow.EditSettingRequest;
import com.shelflife.project.dto.runninglow.RunningLowNotification;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.exception.RunningLowExistsException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.RunningLowSetting;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.RunningLowRepository;

import jakarta.transaction.Transactional;

@Service
public class RunningLowService {
    @Autowired
    private RunningLowRepository repository;

    @Autowired
    private StorageAccessService storageAccessService;

    @Autowired
    private StorageGetterService storageGetterService;

    @Autowired
    private ProductService productService;

    public RunningLowSetting getSetting(final long settingId) throws ItemNotFoundException {
        Optional<RunningLowSetting> setting = repository.findById(settingId);

        if (!setting.isPresent())
            throw new ItemNotFoundException("settingId", "Setting with this id was not found");

        return setting.get();
    }

    public List<RunningLowSetting> getSettingsForStorage(final long storageId, User current)
            throws AccessDeniedException {
        if (!storageAccessService.canAccessStorage(storageId, current))
            throw new AccessDeniedException("You can't access this storage");

        return repository.findByStorageId(storageId);
    }

    public List<RunningLowNotification> getRunningLowInStorage(final long storageId, User current)
            throws AccessDeniedException {
        if (!storageAccessService.canAccessStorage(storageId, current))
            throw new AccessDeniedException("You can't access this storage");

        return repository.findItemsRunningLow(storageId);
    }

    @Transactional
    public RunningLowSetting createSetting(final long storageId, CreateSettingRequest request, User current)
            throws AccessDeniedException, ItemNotFoundException, RunningLowExistsException {
        if (!storageAccessService.canAccessStorage(storageId, current))
            throw new AccessDeniedException("You can't access this storage");

        Storage storage = storageGetterService.getStorage(storageId);
        Product product = productService.getProductByID(request.getProductId());

        if (repository.existsByProductIdAndStorageId(product.getId(), storage.getId()))
            throw new RunningLowExistsException();

        if (request.getRunningLow() < 0)
            throw new IllegalArgumentException("runningLow");

        RunningLowSetting setting = new RunningLowSetting();
        setting.setStorage(storage);
        setting.setProduct(product);
        setting.setRunningLow(request.getRunningLow());

        return repository.save(setting);
    }

    @Transactional
    public RunningLowSetting editSetting(final long settingId, EditSettingRequest request, User current)
            throws AccessDeniedException, IllegalArgumentException, ItemNotFoundException {
        RunningLowSetting setting = getSetting(settingId);

        if (!storageAccessService.canAccessStorage(setting.getStorage().getId(), current))
            throw new AccessDeniedException("You can't access this storage");

        if (request.getRunningLow() < 0)
            throw new IllegalArgumentException("runningLow");

        setting.setRunningLow(request.getRunningLow());
        return repository.save(setting);
    }

    @Transactional
    public void deleteSetting(final long settingId, User current)
            throws ItemNotFoundException, AccessDeniedException {
        RunningLowSetting setting = getSetting(settingId);

        if (!storageAccessService.canAccessStorage(setting.getStorage().getId(), current))
            throw new AccessDeniedException("You can't access this storage");

        repository.delete(setting);
    }
}
