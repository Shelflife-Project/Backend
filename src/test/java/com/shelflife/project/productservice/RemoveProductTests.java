package com.shelflife.project.productservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import com.shelflife.project.model.Product;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ProductRepository;
import com.shelflife.project.service.ProductService;
import com.shelflife.project.service.UserService;

@ExtendWith(MockitoExtension.class)
public class RemoveProductTests {
    @Mock
    ProductRepository repo;

    @Mock
    Authentication auth;

    @Mock
    UserService userService;

    @Spy
    @InjectMocks
    ProductService productService;

    @Test
    void throwsAccessDeniedAsAnonymous() {
        when(userService.getUserByAuth(auth)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> productService.removeProduct(1, auth));
        verifyNoInteractions(repo);
    }

    @Test
    void throwsAccessDeniedAsNonOwner() {
        User user = testUser(1, false);
        User owner = testUser(2, false);
        Product product = testProduct(1, owner);

        when(userService.getUserByAuth(auth)).thenReturn(Optional.of(user));
        when(repo.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(AccessDeniedException.class, () -> productService.removeProduct(1, auth));
        verify(repo, never()).deleteById(any());
    }

    @Test
    void canRemoveAnyAsAdmin() {
        User user = testUser(1, true);
        User owner = testUser(2, false);
        Product product = testProduct(1, owner);

        when(userService.getUserByAuth(auth)).thenReturn(Optional.of(user));
        when(repo.findById(1L)).thenReturn(Optional.of(product));

        assertDoesNotThrow(() -> productService.removeProduct(1, auth));
        verify(repo).deleteById(1L);
    }

    @Test
    void canRemoveAsOwner() {
        User user = testUser(1, false);
        Product product = testProduct(1, user);

        when(userService.getUserByAuth(auth)).thenReturn(Optional.of(user));
        when(repo.findById(1L)).thenReturn(Optional.of(product));

        assertDoesNotThrow(() -> productService.removeProduct(1, auth));
        verify(repo).deleteById(1L);
    }

    @Test
    void throwsItemNotFound() {
        User user = testUser(1, false);

        when(userService.getUserByAuth(auth)).thenReturn(Optional.of(user));

        assertThrows(ItemNotFoundException.class, () -> productService.removeProduct(1, auth));
        verify(repo, never()).deleteById(any());
    }

    private User testUser(long id, boolean admin) {
        User u = new User();
        u.setId(id);
        u.setAdmin(admin);
        u.setEmail("test" + id + "@test.test");
        u.setUsername("test" + id);
        return u;
    }

    private Product testProduct(long id, User owner) {
        Product p = new Product();
        p.setId(id);
        p.setOwner(owner);
        p.setName("name");
        p.setCategory("cat");
        p.setBarcode("12345");
        p.setExpirationDaysDelta(2);
        p.setRunningLow(2);

        return p;
    }
}