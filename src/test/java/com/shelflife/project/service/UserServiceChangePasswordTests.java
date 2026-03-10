package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.shelflife.project.dto.user.ChangePasswordRequest;
import com.shelflife.project.exception.InvalidPasswordException;
import com.shelflife.project.exception.PasswordsDontMatchException;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceChangePasswordTests {
    @Mock
    UserRepository repo;

    @Mock
    PasswordEncoder encoder;

    @Spy
    @InjectMocks
    UserService service;

    @Test
    void throwsInvalidPassword() {
        User user = new User();
        user.setPassword("encoded-old");

        when(encoder.matches("old", "encoded-old"))
                .thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> service.changePassword(validRequest(), user));
        verify(repo, never()).save(any());
    }

    @Test
    void throwsPasswordsDontMatch() {
        User user = new User();
        user.setPassword("encoded-old");

        ChangePasswordRequest req = validRequest();
        req.setNewPasswordRepeat("different");

        when(encoder.matches("old", "encoded-old"))
                .thenReturn(true);

        assertThrows(PasswordsDontMatchException.class, () -> service.changePassword(req, user));
        verify(repo, never()).save(any());
    }

    @Test
    void successfulPasswordChange() {
        User user = new User();
        user.setPassword("encoded-old");

        when(encoder.matches("old", "encoded-old"))
                .thenReturn(true);
        when(encoder.encode("new"))
                .thenReturn("encoded-new");

        service.changePassword(validRequest(), user);

        assertEquals("encoded-new", user.getPassword());
        verify(repo).save(user);
    }

    private ChangePasswordRequest validRequest() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("old");
        req.setNewPassword("new");
        req.setNewPasswordRepeat("new");
        return req;
    }

}
