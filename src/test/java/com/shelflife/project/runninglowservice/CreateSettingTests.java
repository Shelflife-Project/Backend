package com.shelflife.project.runninglowservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import com.shelflife.project.dto.runninglow.CreateSettingRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.exception.RunningLowExistsException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.RunningLowSetting;
import com.shelflife.project.model.Storage;
import com.shelflife.project.repository.RunningLowRepository;
import com.shelflife.project.service.ProductService;
import com.shelflife.project.service.RunningLowService;
import com.shelflife.project.service.StorageMemberService;
import com.shelflife.project.service.StorageService;

@ExtendWith(MockitoExtension.class)
public class CreateSettingTests {
    @Mock
    private RunningLowRepository repository;

    @Mock
    private StorageService storageService;

    @Mock
    private StorageMemberService storageMemberService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private RunningLowService service;

    private Authentication auth;

    @Test
    void successfulCreate() {
        CreateSettingRequest request = new CreateSettingRequest(1, 10);
        Product product = new Product();
        product.setId(1);

        Storage storage = new Storage();
        storage.setId(1);

        doReturn(storage).when(storageService).getStorage(1);
        doReturn(product).when(productService).getProductByID(1);
        doReturn(false).when(repository).existsByProductIdAndStorageId(1, 1);
        doReturn(true).when(storageMemberService).canAccessStorage(1, auth);
        when(repository.save(any(RunningLowSetting.class))).thenAnswer(answer -> answer.getArgument(0));

        RunningLowSetting setting = service.createSetting(1, request, auth);

        assertNotNull(setting);
        assertEquals(1, setting.getStorage().getId());
        assertEquals(1, setting.getProduct().getId());
        assertEquals(10, setting.getRunningLow());
    }

    @Test
    void throwsNotFoundForStorage() {
        CreateSettingRequest request = new CreateSettingRequest(1, 10);

        doThrow(ItemNotFoundException.class).when(storageService).getStorage(1);
        doReturn(true).when(storageMemberService).canAccessStorage(1, auth);
        assertThrows(ItemNotFoundException.class, () -> service.createSetting(1, request, auth));
    }

    @Test
    void throwsNotFoundForProduct() {
        CreateSettingRequest request = new CreateSettingRequest(1, 10);
        Storage storage = new Storage();

        doReturn(storage).when(storageService).getStorage(1);
        doReturn(true).when(storageMemberService).canAccessStorage(1, auth);
        doThrow(ItemNotFoundException.class).when(productService).getProductByID(1);

        assertThrows(ItemNotFoundException.class, () -> service.createSetting(1, request, auth));
    }

    @Test
    void throwsIllegalArgumentForRunningLow() {
        CreateSettingRequest request = new CreateSettingRequest(1, -1);
        Product product = new Product();
        product.setId(1);

        Storage storage = new Storage();
        storage.setId(1);

        doReturn(storage).when(storageService).getStorage(1);
        doReturn(product).when(productService).getProductByID(1);
        doReturn(false).when(repository).existsByProductIdAndStorageId(1, 1);
        doReturn(true).when(storageMemberService).canAccessStorage(1, auth);

        assertThrows(IllegalArgumentException.class, () -> service.createSetting(1, request, auth));
    }

    @Test
    void throwsAlreadyExistsForSetting() {
        CreateSettingRequest request = new CreateSettingRequest(1, -1);
        Product product = new Product();
        product.setId(1);

        Storage storage = new Storage();
        storage.setId(1);

        doReturn(storage).when(storageService).getStorage(1);
        doReturn(product).when(productService).getProductByID(1);
        doReturn(true).when(repository).existsByProductIdAndStorageId(1, 1);
        doReturn(true).when(storageMemberService).canAccessStorage(1, auth);

        assertThrows(RunningLowExistsException.class, () -> service.createSetting(1, request, auth));
    }

    @Test
    void throwsAccessDenied() {
        CreateSettingRequest request = new CreateSettingRequest(1, -1);

        doReturn(false).when(storageMemberService).canAccessStorage(1, auth);

        assertThrows(AccessDeniedException.class, () -> service.createSetting(1, request, auth));
    }
}
