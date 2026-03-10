package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.StorageMember;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageMemberRepository;

@ExtendWith(MockitoExtension.class)
public class StorageMemberServiceDeclineInviteTests {
    @Mock
    private StorageMemberRepository storageMemberRepository;

    @Mock
    private UserService userService;

    @Spy
    @InjectMocks
    private StorageMemberService storageMemberService;

    @Test
    void successfulDecline() {
        User invited = new User();
        invited.setId(1);

        StorageMember member = new StorageMember();
        member.setId(1);
        member.setUser(invited);
        member.setAccepted(false);

        doReturn(member).when(storageMemberService).getMember(1);

        assertDoesNotThrow(() -> storageMemberService.declineInvite(1, invited));
        verify(storageMemberRepository).delete(member);
    }

    @Test
    void throwsAccessDeniedWithNull() {
        assertThrows(AccessDeniedException.class, () -> storageMemberService.declineInvite(1, null));
        verifyNoInteractions(storageMemberRepository);
    }

    @Test
    void throwsItemNotFoundForNoInvite() {
        User user = new User();
        user.setId(1);

        doThrow(ItemNotFoundException.class).when(storageMemberService).getMember(1);

        assertThrows(ItemNotFoundException.class, () -> storageMemberService.declineInvite(1, user));
        verify(storageMemberRepository, never()).save(any());
    }

    @Test
    void throwsAccessDeniedForNotYourInvite() {
        User invited = new User();
        invited.setId(1);

        User user = new User();
        user.setId(2);

        StorageMember member = new StorageMember();
        member.setId(1);
        member.setUser(invited);
        member.setAccepted(false);

        doReturn(member).when(storageMemberService).getMember(1);

        assertThrows(AccessDeniedException.class, () -> storageMemberService.declineInvite(1, user));
        verify(storageMemberRepository, never()).save(any());
    }
}
