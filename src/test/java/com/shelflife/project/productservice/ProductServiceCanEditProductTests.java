package com.shelflife.project.productservice;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import com.shelflife.project.model.Product;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ProductRepository;
import com.shelflife.project.service.ProductService;
import com.shelflife.project.service.UserService;

@ExtendWith(MockitoExtension.class)
public class ProductServiceCanEditProductTests {
    @Mock
    private UserService userService;

    @Mock
    private ProductRepository productRepository;

    @Spy
    @InjectMocks
    private ProductService productService;

    private Authentication auth;

    @Test
    void returnsFalseAsAnonymous() {
        doThrow(AccessDeniedException.class).when(userService).getUserByAuth(auth);
        assertFalse(productService.canEditProduct(1L, auth));
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
        doReturn(admin).when(userService).getUserByAuth(auth);

        assertTrue(productService.canEditProduct(1L, auth));
    }

    @Test
    void returnsTrueAsOwner() {
        User owner = new User();
        owner.setId(1);

        Product product = new Product();
        product.setOwner(owner);

        doReturn(product).when(productService).getProductByID(1);
        doReturn(owner).when(userService).getUserByAuth(auth);

        assertTrue(productService.canEditProduct(1L, auth));
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
        doReturn(user).when(userService).getUserByAuth(auth);

        assertFalse(productService.canEditProduct(1L, auth));
    }
}
