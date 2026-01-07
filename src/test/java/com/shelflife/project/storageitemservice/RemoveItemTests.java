package com.shelflife.project.storageitemservice;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.StorageItem;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageItemRepository;
import com.shelflife.project.service.StorageItemService;
import com.shelflife.project.service.StorageMemberService;
import com.shelflife.project.service.UserService;

@ExtendWith(MockitoExtension.class)
public class RemoveItemTests {
    @Mock
    private StorageItemRepository storageItemRepository;

    @Mock
    private UserService userService;

    @Mock
    private StorageMemberService storageMemberService;

    @Spy
    @InjectMocks
    private StorageItemService storageItemService;

    private Authentication auth;

    @Test
    void successfulRemove() {
        User user = new User();
        user.setId(1);

        Storage storage = new Storage();
        storage.setId(2);

        StorageItem item = new StorageItem();
        item.setId(3);
        item.setStorage(storage);

        when(userService.getUserByAuth(auth)).thenReturn(user);
        when(storageItemRepository.findById(3L)).thenReturn(Optional.of(item));

        doReturn(true).when(storageMemberService).canAccessStorage(2, user.getId());

        storageItemService.removeItemFromStorage(3, auth);

        verify(storageItemRepository).deleteById(3L);
    }

    @Test
    void throwsItemNotFound() {
        when(userService.getUserByAuth(auth)).thenReturn(new User());
        when(storageItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> storageItemService.removeItemFromStorage(1L, auth));

        verify(storageItemRepository, never()).deleteById(anyLong());
    }

    @Test
    void throwsAccessDenied() {
        User user = new User();
        user.setId(1);

        Storage storage = new Storage();
        storage.setId(2);

        StorageItem item = new StorageItem();
        item.setId(3);
        item.setStorage(storage);

        when(userService.getUserByAuth(auth)).thenReturn(user);
        when(storageItemRepository.findById(3L)).thenReturn(Optional.of(item));
        doReturn(false).when(storageMemberService).canAccessStorage(2, user.getId());

        assertThrows(AccessDeniedException.class, () -> storageItemService.removeItemFromStorage(3, auth));
        verify(storageItemRepository, never()).deleteById(anyLong());
    }

}
