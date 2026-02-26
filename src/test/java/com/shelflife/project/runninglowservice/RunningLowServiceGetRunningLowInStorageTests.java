package com.shelflife.project.runninglowservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import com.shelflife.project.model.Product;
import com.shelflife.project.repository.RunningLowRepository;
import com.shelflife.project.service.RunningLowService;
import com.shelflife.project.service.StorageAccessService;

@ExtendWith(MockitoExtension.class)
public class RunningLowServiceGetRunningLowInStorageTests {
    @Mock
    private RunningLowRepository repository;

    @Mock
    private StorageAccessService storageAccessService;

    @InjectMocks
    private RunningLowService service;

    private Authentication auth;

    @Test
    void successfulGet() {
        Product product = new Product();
        doReturn(true).when(storageAccessService).canAccessStorage(1, auth);
        doReturn(List.of(product)).when(repository).findItemsRunningLow(1);

        assertDoesNotThrow(() -> service.getRunningLowInStorage(1, auth));
        assertEquals(product, service.getRunningLowInStorage(1, auth).get(0));
    }

    @Test
    void throwsAccessDenied() {
        doReturn(false).when(storageAccessService).canAccessStorage(1, auth);

        assertThrows(AccessDeniedException.class, () -> service.getRunningLowInStorage(1, auth));
    }
}
