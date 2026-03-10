package com.shelflife.project.service;

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

import com.shelflife.project.model.Product;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.RunningLowRepository;

@ExtendWith(MockitoExtension.class)
public class RunningLowServiceGetRunningLowInStorageTests {
    @Mock
    private RunningLowRepository repository;

    @Mock
    private StorageAccessService storageAccessService;

    @InjectMocks
    private RunningLowService service;

    private User user = new User();

    @Test
    void successfulGet() {
        Product product = new Product();
        doReturn(true).when(storageAccessService).canAccessStorage(1, user);
        doReturn(List.of(product)).when(repository).findItemsRunningLow(1);

        assertDoesNotThrow(() -> service.getRunningLowInStorage(1, user));
        assertEquals(product, service.getRunningLowInStorage(1, user).get(0));
    }

    @Test
    void throwsAccessDenied() {
        doReturn(false).when(storageAccessService).canAccessStorage(1, user);

        assertThrows(AccessDeniedException.class, () -> service.getRunningLowInStorage(1, user));
    }
}
