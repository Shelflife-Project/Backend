package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.StorageMember;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageMemberRepository;
import com.shelflife.project.repository.StorageRepository;

@ExtendWith(MockitoExtension.class)
public class StorageMemberServiceGetStorageMembersTests {

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private StorageMemberRepository storageMemberRepository;

    @Mock
    private StorageAccessService storageAccessService;

    @Mock
    private UserService userService;

    @InjectMocks
    private StorageMemberService storageMemberService;

    private User testUser;
    private User member1User;
    private User member2User;
    private Storage testStorage;
    private StorageMember member1;
    private StorageMember member2;
    private long storageId = 1L;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        member1User = new User();
        member1User.setId(2L);
        member1User.setUsername("member1");

        member2User = new User();
        member2User.setId(3L);
        member2User.setUsername("member2");

        testStorage = new Storage();
        testStorage.setId(storageId);
        testStorage.setName("Test Storage");

        member1 = new StorageMember();
        member1.setId(1L);
        member1.setUser(member1User);
        member1.setStorage(testStorage);
        member1.setAccepted(true);

        member2 = new StorageMember();
        member2.setId(2L);
        member2.setUser(member2User);
        member2.setStorage(testStorage);
        member2.setAccepted(true);
    }

    @Test
    void successfulGet() {
        List<StorageMember> expectedMembers = Arrays.asList(member1, member2);
        
        when(storageRepository.existsById(storageId)).thenReturn(true);
        when(storageAccessService.canAccessStorage(storageId, testUser)).thenReturn(true);
        when(storageMemberRepository.findByStorageId(storageId)).thenReturn(expectedMembers);

        List<StorageMember> result = assertDoesNotThrow(() -> 
            storageMemberService.getStorageMembers(storageId, testUser));

        assertEquals(expectedMembers, result);
        assertEquals(2, result.size());
    }

    @Test
    void throwsNotFoundForStorage() {
        when(storageRepository.existsById(storageId)).thenReturn(false);

        assertThrows(ItemNotFoundException.class, () -> 
            storageMemberService.getStorageMembers(storageId, testUser));
    }

    @Test
    void throwsAccessDenied() {
        when(storageRepository.existsById(storageId)).thenReturn(true);
        when(storageAccessService.canAccessStorage(storageId, testUser)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> 
            storageMemberService.getStorageMembers(storageId, testUser));
    }
}
