package com.shelflife.project.service;

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

import com.shelflife.project.dto.runninglow.CreateSettingRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.exception.RunningLowExistsException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.RunningLowSetting;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.RunningLowRepository;

@ExtendWith(MockitoExtension.class)
public class RunningLowServiceCreateSettingTests {
    @Mock
    private RunningLowRepository repository;

    @Mock
    private StorageService storageService;

    @Mock
    private StorageGetterService storageGetterService;

    @Mock
    private StorageAccessService storageAccessService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private RunningLowService service;

    User user = new User();

    @Test
    void successfulCreate_withTen() {
        CreateSettingRequest request = new CreateSettingRequest(1, 10);

        Product product = new Product();
        product.setId(1);

        Storage storage = new Storage();
        storage.setId(1);

        doReturn(storage).when(storageGetterService).getStorage(1);
        doReturn(product).when(productService).getProductByID(1);
        doReturn(false).when(repository).existsByProductIdAndStorageId(1, 1);
        doReturn(true).when(storageAccessService).canAccessStorage(1, user);
        when(repository.save(any(RunningLowSetting.class))).thenAnswer(answer -> answer.getArgument(0));

        RunningLowSetting setting = service.createSetting(1, request, user);

        assertNotNull(setting);
        assertEquals(1, setting.getStorage().getId());
        assertEquals(1, setting.getProduct().getId());
        assertEquals(10, setting.getRunningLow());
    }

    @Test
    void successfulCreate_withZero() {
        CreateSettingRequest request = new CreateSettingRequest(1, 0);

        Product product = new Product();
        product.setId(1);

        Storage storage = new Storage();
        storage.setId(1);

        doReturn(storage).when(storageGetterService).getStorage(1);
        doReturn(product).when(productService).getProductByID(1);
        doReturn(false).when(repository).existsByProductIdAndStorageId(1, 1);
        doReturn(true).when(storageAccessService).canAccessStorage(1, user);
        when(repository.save(any(RunningLowSetting.class))).thenAnswer(answer -> answer.getArgument(0));

        RunningLowSetting setting = service.createSetting(1, request, user);

        assertNotNull(setting);
        assertEquals(1, setting.getStorage().getId());
        assertEquals(1, setting.getProduct().getId());
        assertEquals(0, setting.getRunningLow());
    }

    @Test
    void throwsNotFoundForStorage() {
        CreateSettingRequest request = new CreateSettingRequest(1, 10);

        doThrow(ItemNotFoundException.class).when(storageGetterService).getStorage(1);
        doReturn(true).when(storageAccessService).canAccessStorage(1, user);
        assertThrows(ItemNotFoundException.class, () -> service.createSetting(1, request, user));
    }

    @Test
    void throwsNotFoundForProduct() {
        CreateSettingRequest request = new CreateSettingRequest(1, 10);
        Storage storage = new Storage();

        doReturn(storage).when(storageGetterService).getStorage(1);
        doReturn(true).when(storageAccessService).canAccessStorage(1, user);
        doThrow(ItemNotFoundException.class).when(productService).getProductByID(1);

        assertThrows(ItemNotFoundException.class, () -> service.createSetting(1, request, user));
    }

    @Test
    void throwsIllegalArgumentForRunningLow() {
        CreateSettingRequest request = new CreateSettingRequest(1, -1);
        
        Product product = new Product();
        product.setId(1);

        Storage storage = new Storage();
        storage.setId(1);

        doReturn(storage).when(storageGetterService).getStorage(1);
        doReturn(product).when(productService).getProductByID(1);
        doReturn(false).when(repository).existsByProductIdAndStorageId(1, 1);
        doReturn(true).when(storageAccessService).canAccessStorage(1, user);

        assertThrows(IllegalArgumentException.class, () -> service.createSetting(1, request, user));
    }

    @Test
    void throwsAlreadyExistsForSetting() {
        CreateSettingRequest request = new CreateSettingRequest(1, -1);
        
        Product product = new Product();
        product.setId(1);

        Storage storage = new Storage();
        storage.setId(1);

        doReturn(storage).when(storageGetterService).getStorage(1);
        doReturn(product).when(productService).getProductByID(1);
        doReturn(true).when(repository).existsByProductIdAndStorageId(1, 1);
        doReturn(true).when(storageAccessService).canAccessStorage(1, user);

        assertThrows(RunningLowExistsException.class, () -> service.createSetting(1, request, user));
    }

    @Test
    void throwsAccessDenied() {
        CreateSettingRequest request = new CreateSettingRequest(1, -1);

        doReturn(false).when(storageAccessService).canAccessStorage(1, null);
        assertThrows(AccessDeniedException.class, () -> service.createSetting(1, request, null));
    }
}
