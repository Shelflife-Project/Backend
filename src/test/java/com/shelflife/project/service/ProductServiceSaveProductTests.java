package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import com.shelflife.project.dto.product.CreateProductRequest;
import com.shelflife.project.exception.BarcodeExistsException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
public class ProductServiceSaveProductTests {
    @Mock
    private ProductRepository repo;

    @Mock
    private Authentication auth;

    @Spy
    @InjectMocks
    private ProductService productService;

    @Test
    void successfulCreation() {
        User user = testUser(1, false);

        when(repo.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product p = productService.saveProduct(validRequest(), user);
        assertNotNull(p);
        assertEquals(1, p.getOwnerId());

        verify(repo).save(any(Product.class));
    }

    @Test
    void throwsAccessDeniedWithNull() {
        assertThrows(AccessDeniedException.class, () -> productService.saveProduct(validRequest(), null));
        verifyNoInteractions(repo);
    }

    @Test
    void throwsBarcodeExists() {
        User user = testUser(1, false);
        when(repo.existsByBarcode(validRequest().getBarcode())).thenReturn(true);

        assertThrows(BarcodeExistsException.class, () -> {
            productService.saveProduct(validRequest(), user);
        });

        verify(repo, never()).save(any());
    }

    @Test
    void throwsIllegalArgumentForNullCategory() {
        User user = testUser(1, false);

        CreateProductRequest request = validRequest();
        request.setCategory(null);

        assertThrows(IllegalArgumentException.class, () -> {
            productService.saveProduct(request, user);
        });

        verify(repo, never()).save(any());
    }

    @Test
    void throwsIllegalArgumentForEmptyCategory() {
        User user = testUser(1, false);

        CreateProductRequest request = validRequest();
        request.setCategory("");

        assertThrows(IllegalArgumentException.class, () -> {
            productService.saveProduct(request, user);
        });

        verify(repo, never()).save(any());
    }

    @Test
    void throwsIllegalArgumentForNullName() {
        User user = testUser(1, false);

        CreateProductRequest request = validRequest();
        request.setName(null);

        assertThrows(IllegalArgumentException.class, () -> {
            productService.saveProduct(request, user);
        });

        verify(repo, never()).save(any());
    }

    @Test
    void throwsIllegalArgumentForEmptyName() {
        User user = testUser(1, false);

        CreateProductRequest request = validRequest();
        request.setName("");

        assertThrows(IllegalArgumentException.class, () -> {
            productService.saveProduct(request, user);
        });

        verify(repo, never()).save(any());
    }

    @Test
    void throwsIllegalArgumentForZeroExpiration() {
        User user = testUser(1, false);

        CreateProductRequest request = validRequest();
        request.setExpirationDaysDelta(0);

        assertThrows(IllegalArgumentException.class, () -> {
            productService.saveProduct(request, user);
        });

        verify(repo, never()).save(any());
    }

    @Test
    void throwsIllegalArgumentForNegativeExpiration() {
        User user = testUser(1, false);

        CreateProductRequest request = validRequest();
        request.setExpirationDaysDelta(-5);

        assertThrows(IllegalArgumentException.class, () -> {
            productService.saveProduct(request, user);
        });

        verify(repo, never()).save(any());
    }

    private CreateProductRequest validRequest() {
        CreateProductRequest req = new CreateProductRequest();
        req.setName("ProductName");
        req.setDescription("Good product");
        req.setExpirationDaysDelta(365);
        req.setCategory("Canned soup");
        req.setBarcode("12346");
        return req;
    }

    private User testUser(long id, boolean admin) {
        User u = new User();
        u.setId(id);
        u.setAdmin(admin);
        u.setEmail("test" + id + "@test.test");
        u.setUsername("test" + id);
        return u;
    }

}
