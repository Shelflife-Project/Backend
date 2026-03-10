package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageRepository;

@ExtendWith(MockitoExtension.class)
public class StorageAccessServiceTests {
    @Mock
    private StorageRepository storageRepository;

    @Mock
    private UserService userService;

    @Spy
    @InjectMocks
    private StorageAccessService storageAccessService;

    @Test
    void returnsFalseWithNull() {
        assertFalse(storageAccessService.canAccessStorage(1, null));
    }

    @Test
    void returnsTrueAsAdmin() {
        User user = new User();
        user.setAdmin(true);

        assertTrue(storageAccessService.canAccessStorage(1, user));
    }

    @Test
    void callsRepoFunction() {
        User user = new User();
        user.setId(1);
        user.setAdmin(false);

        storageAccessService.canAccessStorage(1, user);

        verify(storageRepository).isMemberOrOwner(1, 1);
    }
}
