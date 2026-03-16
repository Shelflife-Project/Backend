package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import com.shelflife.project.dto.runninglow.RunningLowNotification;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.RunningLowRepository;

@ExtendWith(MockitoExtension.class)
class RunningLowServiceGetRunningLowByUserTests {

    @InjectMocks
    private RunningLowService runningLowService;

    @Mock
    private RunningLowRepository runningLowRepository;

    @Mock
    private StorageGetterService storageGetterService;

    private User user = new User();

    @Test
    void testGetRunningLowByUser_ValidUserWithAccess() {
        user.setId(1);

        Storage s1 = new Storage();
        s1.setId(1);

        Storage s2 = new Storage();
        s2.setId(2);

        RunningLowNotification n1 = new RunningLowNotification(s1, null, 0, 0);
        RunningLowNotification n2 = new RunningLowNotification(s2, null, 0, 0);

        Page<Storage> pagedStorages = new PageImpl<>(List.of(s1, s2));
        when(storageGetterService.getStorages(user, "", Pageable.unpaged())).thenReturn(pagedStorages);

        List<RunningLowNotification> notifications1 = List.of(n1);
        List<RunningLowNotification> notifications2 = List.of(n2);

        when(runningLowRepository.findItemsRunningLow(1L)).thenReturn(notifications1);
        when(runningLowRepository.findItemsRunningLow(2L)).thenReturn(notifications2);

        List<RunningLowNotification> result = runningLowService.getRunningLowByUser(user);

        assertEquals(2, result.size());
        assertTrue(result.containsAll(notifications1));
        assertTrue(result.containsAll(notifications2));
    }

    @Test
    void testGetRunningLowByUser_UserWithNoAccess() {
        user.setId(1);

        when(storageGetterService.getStorages(user, "", Pageable.unpaged()))
                .thenThrow(new AccessDeniedException(null));

        assertThrows(AccessDeniedException.class, () -> runningLowService.getRunningLowByUser(user));
    }

    @Test
    void testGetRunningLowByUser_EmptyStorages() {
        user.setId(1);

        Page<Storage> storages = new PageImpl<>(new ArrayList<>());
        when(storageGetterService.getStorages(user, "", Pageable.unpaged())).thenReturn(storages);

        List<RunningLowNotification> result = runningLowService.getRunningLowByUser(user);

        assertTrue(result.isEmpty());
    }
}