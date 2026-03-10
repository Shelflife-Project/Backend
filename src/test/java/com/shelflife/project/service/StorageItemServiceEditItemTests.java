package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.shelflife.project.dto.storage.EditItemRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.StorageItem;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageItemRepository;

@ExtendWith(MockitoExtension.class)
public class StorageItemServiceEditItemTests {

    @Mock
    private StorageItemRepository storageItemRepository;

    @Mock
    private StorageAccessService storageAccessService;

    @Spy
    @InjectMocks
    private StorageItemService storageItemService;

    private User user = new User();

    @Test
    void successfulForTomorrow() {
        Storage storage = new Storage();
        storage.setId(1);

        StorageItem item = new StorageItem();
        item.setId(1);
        item.setStorage(storage);
        item.setExpiresAt(LocalDate.now());

        doReturn(Optional.of(item)).when(storageItemRepository).findById(1L);
        doReturn(true).when(storageAccessService).canAccessStorage(1, user);
        when(storageItemRepository.save(any(StorageItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StorageItem result = storageItemService.editItem(1, validRequest(LocalDate.now().plusDays(1)), user);

        assertEquals(LocalDate.now().plusDays(1), result.getExpiresAt());

        verify(storageItemRepository).save(any(StorageItem.class));
    }

    @Test
    void throwsIllegalArgumentForYesterday() {
        Storage storage = new Storage();
        storage.setId(1);

        StorageItem item = new StorageItem();
        item.setId(1);
        item.setStorage(storage);
        item.setExpiresAt(LocalDate.now());

        EditItemRequest request = validRequest(LocalDate.now().minusDays(1));

        doReturn(Optional.of(item)).when(storageItemRepository).findById(1L);
        doReturn(true).when(storageAccessService).canAccessStorage(1, user);

        assertThrows(IllegalArgumentException.class, () -> storageItemService.editItem(1, request, user));

        verify(storageItemRepository, never()).save(any());
    }

    @Test
    void successfulForToday() {
        Storage storage = new Storage();
        storage.setId(1);

        StorageItem item = new StorageItem();
        item.setId(1);
        item.setStorage(storage);
        item.setExpiresAt(LocalDate.now());

        doReturn(Optional.of(item)).when(storageItemRepository).findById(1L);
        doReturn(true).when(storageAccessService).canAccessStorage(1, user);
        when(storageItemRepository.save(any(StorageItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EditItemRequest request = validRequest(LocalDate.now());
        StorageItem result = storageItemService.editItem(1, request, user);
        assertEquals(LocalDate.now(), result.getExpiresAt());

        verify(storageItemRepository).save(any(StorageItem.class));
    }

    @Test
    void throwsAccessDenied() {
        Storage storage = new Storage();
        storage.setId(1);

        StorageItem item = new StorageItem();
        item.setId(1);
        item.setStorage(storage);
        item.setExpiresAt(LocalDate.now());

        doReturn(Optional.of(item)).when(storageItemRepository).findById(1L);
        doReturn(false).when(storageAccessService).canAccessStorage(1, user);

        assertThrows(AccessDeniedException.class,
                () -> storageItemService.editItem(1, validRequest(LocalDate.now()), user));

        verify(storageItemRepository, never()).save(any());
    }

    @Test
    void throwsItemNotFound() {
        doReturn(Optional.empty()).when(storageItemRepository).findById(1L);
        assertThrows(ItemNotFoundException.class,
                () -> storageItemService.editItem(1, validRequest(LocalDate.now()), user));

        verify(storageItemRepository, never()).save(any());
    }

    EditItemRequest validRequest(LocalDate date) {
        EditItemRequest request = new EditItemRequest();
        request.setExpiresAt(date);

        return request;
    }
}
