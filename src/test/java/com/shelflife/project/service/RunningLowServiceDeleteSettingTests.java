package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.RunningLowSetting;
import com.shelflife.project.model.Storage;
import com.shelflife.project.repository.RunningLowRepository;

@ExtendWith(MockitoExtension.class)
public class RunningLowServiceDeleteSettingTests {
    @Mock
    private RunningLowRepository repository;

    @Mock
    private StorageService storageService;

    @Mock
    private StorageAccessService storageAccessService;

    @Mock
    private ProductService productService;

    @InjectMocks
    @Spy
    private RunningLowService service;

    private Authentication auth;

    @Test
    void successfulDelete() {
        Product product = new Product();
        product.setId(1);

        Storage storage = new Storage();
        storage.setId(1);

        RunningLowSetting setting = new RunningLowSetting();
        setting.setId(1);
        setting.setProduct(product);
        setting.setStorage(storage);
        setting.setRunningLow(4);

        doReturn(setting).when(service).getSetting(1);
        doReturn(true).when(storageAccessService).canAccessStorage(1, auth);

        assertDoesNotThrow(() -> service.deleteSetting(1, auth));
        verify(repository).delete(setting);
    }

    @Test
    void throwsNotFound() {
        doThrow(ItemNotFoundException.class).when(service).getSetting(1);
        assertThrows(ItemNotFoundException.class, () -> service.deleteSetting(1, auth));
    }

    @Test
    void throwsAccessDenied() {
        Product product = new Product();
        product.setId(1);

        Storage storage = new Storage();
        storage.setId(1);

        RunningLowSetting setting = new RunningLowSetting();
        setting.setId(1);
        setting.setProduct(product);
        setting.setStorage(storage);
        setting.setRunningLow(4);

        doReturn(setting).when(service).getSetting(1);
        doReturn(false).when(storageAccessService).canAccessStorage(1, auth);

        assertThrows(AccessDeniedException.class, () -> service.deleteSetting(1, auth));
        verify(repository, never()).delete(setting);
    }
}
