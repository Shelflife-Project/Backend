package com.shelflife.project.service;

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

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
public class ProductServiceRemoveProductTests {
    @Mock
    ProductRepository repo;

    @Spy
    @InjectMocks
    ProductService productService;

    @Test
    void throwsAccessDeniedWithNull() {
        assertThrows(AccessDeniedException.class, () -> productService.removeProduct(1, null));
        verifyNoInteractions(repo);
    }

    @Test
    void throwsAccessDeniedAsNonOwner() {
        User user = testUser(1, false);
        User owner = testUser(2, false);
        Product product = testProduct(1, owner);

        when(repo.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(AccessDeniedException.class, () -> productService.removeProduct(1, user));
        verify(repo, never()).deleteById(any());
    }

    @Test
    void canRemoveAnyAsAdmin() {
        User user = testUser(1, true);
        User owner = testUser(2, false);
        Product product = testProduct(1, owner);

        when(repo.findById(1L)).thenReturn(Optional.of(product));

        assertDoesNotThrow(() -> productService.removeProduct(1, user));
        verify(repo).deleteById(1L);
    }

    @Test
    void canRemoveAsOwner() {
        User user = testUser(1, false);
        Product product = testProduct(1, user);

        when(repo.findById(1L)).thenReturn(Optional.of(product));

        assertDoesNotThrow(() -> productService.removeProduct(1, user));
        verify(repo).deleteById(1L);
    }

    @Test
    void throwsItemNotFound() {
        User user = testUser(1, false);

        assertThrows(ItemNotFoundException.class, () -> productService.removeProduct(1, user));
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

        return p;
    }
}