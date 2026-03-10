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

import com.shelflife.project.model.RunningLowSetting;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.RunningLowRepository;

@ExtendWith(MockitoExtension.class)
public class RunningLowServiceGetSettingsForStorageTests {
    @Mock
    private RunningLowRepository repository;

    @Mock
    private StorageAccessService storageAccessService;

    @InjectMocks
    private RunningLowService service;

    private User user = new User();

    @Test
    void successfulGet() {
        RunningLowSetting setting = new RunningLowSetting();
        doReturn(true).when(storageAccessService).canAccessStorage(1, user);
        doReturn(List.of(setting)).when(repository).findByStorageId(1);

        assertDoesNotThrow(() -> service.getSettingsForStorage(1, user));
        assertEquals(setting, service.getSettingsForStorage(1, user).get(0));
    }

    @Test
    void throwsAccessDenied() {
        doReturn(false).when(storageAccessService).canAccessStorage(1, user);

        assertThrows(AccessDeniedException.class, () -> service.getSettingsForStorage(1, user));
    }
}
