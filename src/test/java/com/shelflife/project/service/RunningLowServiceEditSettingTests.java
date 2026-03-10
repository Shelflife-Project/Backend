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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.shelflife.project.dto.runninglow.EditSettingRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.RunningLowSetting;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.RunningLowRepository;

@ExtendWith(MockitoExtension.class)
public class RunningLowServiceEditSettingTests {
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

    private User user = new User();

    @Test
    void successfulEdit_withTen() {
        EditSettingRequest request = new EditSettingRequest(10);
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
        doReturn(true).when(storageAccessService).canAccessStorage(1, user);
        when(repository.save(any(RunningLowSetting.class))).thenAnswer(answer -> answer.getArgument(0));

        RunningLowSetting edited = service.editSetting(1, request, user);

        assertNotNull(edited);
        assertEquals(1, edited.getStorage().getId());
        assertEquals(1, edited.getProduct().getId());
        assertEquals(10, edited.getRunningLow());
    }

    @Test
    void successfulEdit_withZero() {
        EditSettingRequest request = new EditSettingRequest(0);
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
        doReturn(true).when(storageAccessService).canAccessStorage(1, user);
        when(repository.save(any(RunningLowSetting.class))).thenAnswer(answer -> answer.getArgument(0));

        RunningLowSetting edited = service.editSetting(1, request, user);

        assertNotNull(edited);
        assertEquals(1, edited.getStorage().getId());
        assertEquals(1, edited.getProduct().getId());
        assertEquals(0, edited.getRunningLow());
    }

    @Test
    void throwsNotFound() {
        EditSettingRequest request = new EditSettingRequest(10);

        doThrow(ItemNotFoundException.class).when(service).getSetting(1);
        assertThrows(ItemNotFoundException.class, () -> service.editSetting(1, request, user));
    }

    @Test
    void throwsIllegalArgumentForRunningLow() {
        EditSettingRequest request = new EditSettingRequest(-1);
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
        doReturn(true).when(storageAccessService).canAccessStorage(1, user);
        assertThrows(IllegalArgumentException.class, () -> service.editSetting(1, request, user));
    }

    @Test
    void throwsAccessDenied() {
        EditSettingRequest request = new EditSettingRequest(5);
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

        doReturn(false).when(storageAccessService).canAccessStorage(1, user);
        assertThrows(AccessDeniedException.class, () -> service.editSetting(1, request, user));
    }
}
