package com.shelflife.project.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.shelflife.project.dto.product.CreateProductRequest;
import com.shelflife.project.dto.product.UpdateProductRequest;
import com.shelflife.project.exception.BarcodeExistsException;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.filter.ProductFilter;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ProductRepository;

import jakarta.transaction.Transactional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserService userService;

    public List<Product> findProducts(ProductFilter filter) {

        if (filter.getName() != null && filter.getCategory() != null && filter.getBarcode() != null) {
            return productRepository
                    .findByNameContainingIgnoreCaseAndCategoryContainingIgnoreCaseAndBarcodeContainingIgnoreCase(
                            filter.getName(), filter.getCategory(), filter.getBarcode());
        }

        if (filter.getName() != null && filter.getCategory() != null)
            return productRepository.findByNameContainingIgnoreCaseAndCategoryContainingIgnoreCase(filter.getName(),
                    filter.getCategory());

        if (filter.getName() != null)
            return productRepository.findByNameContainingIgnoreCase(filter.getName());

        if (filter.getCategory() != null)
            return productRepository.findByCategoryContainingIgnoreCase(filter.getCategory());

        if (filter.getBarcode() != null)
            return productRepository.findByBarcodeContainingIgnoreCase(filter.getBarcode());

        return productRepository.findAll();
    }

    public List<String> getCategories() {
        return productRepository.getCategories();
    }

    public Product getProductByID(final long id) throws ItemNotFoundException {
        Optional<Product> product = productRepository.findById(id);

        if (!product.isPresent())
            throw new ItemNotFoundException("id", "Product with this id was not found");

        return product.get();
    }

    public boolean productExistsByID(final long id) {
        return productRepository.existsById(id);
    }

    public boolean canEditProduct(final long productId, Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            Product p = getProductByID(productId);

            return p.getOwnerId() == user.getId() || user.isAdmin();

        } catch (ItemNotFoundException | AccessDeniedException e) {
            return false;
        }
    }

    @Transactional
    public Product saveProduct(CreateProductRequest request, Authentication auth)
            throws AccessDeniedException, BarcodeExistsException, IllegalArgumentException {

        User currentUser = userService.getUserByAuth(auth);
        Product product = new Product();

        product.setOwner(currentUser);

        if (request.getBarcode() != null) {
            if (!request.getBarcode().isBlank()) {
                if (productRepository.existsByBarcode(request.getBarcode()))
                    throw new BarcodeExistsException(request.getBarcode());

                product.setBarcode(request.getBarcode());
            }
        }

        if (request.getCategory() == null)
            throw new IllegalArgumentException("category");

        if (request.getCategory().isBlank())
            throw new IllegalArgumentException("category");

        product.setCategory(request.getCategory());

        if (request.getName() == null)
            throw new IllegalArgumentException("name");

        if (request.getName().isBlank())
            throw new IllegalArgumentException("name");

        product.setName(request.getName());

        if (request.getExpirationDaysDelta() < 1)
            throw new IllegalArgumentException("expirationDaysDelta");

        product.setExpirationDaysDelta(request.getExpirationDaysDelta());

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(long productId, UpdateProductRequest request, Authentication auth)
            throws BarcodeExistsException, AccessDeniedException, IllegalArgumentException {
        if (!canEditProduct(productId, auth))
            throw new AccessDeniedException(null);

        Product productDB = getProductByID(productId);
        if (request.getName() != null) {
            if (request.getName().isBlank())
                throw new IllegalArgumentException("name");

            productDB.setName(request.getName());
        }

        if (request.getCategory() != null) {
            if (request.getCategory().isBlank())
                throw new IllegalArgumentException("category");

            productDB.setCategory(request.getCategory());
        }

        if (request.getBarcode() != null) {
            if (request.getBarcode().isBlank())
                throw new IllegalArgumentException("barcode");

            if (productRepository.existsByBarcode(request.getBarcode()))
                throw new BarcodeExistsException(request.getBarcode());

            productDB.setBarcode(request.getBarcode());
        }

        if (request.getExpirationDaysDelta() != null) {
            if (request.getExpirationDaysDelta() < 1)
                throw new IllegalArgumentException("expirationDaysDelta");

            productDB.setExpirationDaysDelta(request.getExpirationDaysDelta());
        }

        return productRepository.save(productDB);
    }

    @Transactional
    public void removeProduct(long id, Authentication auth) throws AccessDeniedException, ItemNotFoundException {
        User currentUser = userService.getUserByAuth(auth);

        Product product = getProductByID(id);
        if (!currentUser.isAdmin() && currentUser.getId() != product.getOwnerId())
            throw new AccessDeniedException(null);

        productRepository.deleteById(id);
    }
}
