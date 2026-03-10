package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shelflife.project.model.Product;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
public class ProductServiceCanEditProductTests {
    @Mock
    private UserService userService;

    @Mock
    private ProductRepository productRepository;

    @Spy
    @InjectMocks
    private ProductService productService;

    @Test
    void returnsFalseWithNull() {
        assertFalse(productService.canEditProduct(1L, null));
        verifyNoInteractions(productRepository);
    }

    @Test
    void returnsTrueAsAdmin() {
        User admin = new User();
        admin.setId(1);
        admin.setAdmin(true);

        User owner = new User();
        owner.setId(2);

        Product product = new Product();
        product.setOwner(owner);

        doReturn(product).when(productService).getProductByID(1);
        assertTrue(productService.canEditProduct(1L, admin));
    }

    @Test
    void returnsTrueAsOwner() {
        User owner = new User();
        owner.setId(1);

        Product product = new Product();
        product.setOwner(owner);

        doReturn(product).when(productService).getProductByID(1);
        assertTrue(productService.canEditProduct(1L, owner));
    }

    @Test
    void returnsFalseAsNotOwner() {
        User user = new User();
        user.setId(1);
        user.setAdmin(false);

        User owner = new User();
        owner.setId(2);

        Product product = new Product();
        product.setOwner(owner);

        doReturn(product).when(productService).getProductByID(1);
        assertFalse(productService.canEditProduct(1L, user));
    }
}
