package com.shelflife.project.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.shelflife.project.dto.product.CreateProductRequest;
import com.shelflife.project.dto.product.UpdateProductRequest;
import com.shelflife.project.exception.BarcodeExistsException;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ProductRepository;

import jakarta.transaction.Transactional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable).toList();
    }

    public Page<Product> findProducts(String search, Pageable pageable) {
        if(!search.isBlank())
            return productRepository.searchProducts(search, pageable);

        return productRepository.findAll(pageable);
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

    public boolean canEditProduct(final long productId, User user) {
        if(user == null)
            return false;
        
        try {
            Product p = getProductByID(productId);
            return p.getOwnerId() == user.getId() || user.isAdmin();
        } catch (ItemNotFoundException | AccessDeniedException e) {
            return false;
        }
    }

    @Transactional
    public Product saveProduct(CreateProductRequest request, User currentUser)
            throws AccessDeniedException, BarcodeExistsException, IllegalArgumentException {

        if(currentUser == null)
            throw new AccessDeniedException(null);

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
        product.setDescription(request.getDescription());

        if (request.getExpirationDaysDelta() < 1)
            throw new IllegalArgumentException("expirationDaysDelta");

        product.setExpirationDaysDelta(request.getExpirationDaysDelta());

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(long productId, UpdateProductRequest request, User currentUser)
            throws BarcodeExistsException, AccessDeniedException, IllegalArgumentException, ItemNotFoundException {
        if (!canEditProduct(productId, currentUser))
            throw new AccessDeniedException(null);

        Product productDB = getProductByID(productId);
        if (request.getName() != null) {
            if (request.getName().isBlank())
                throw new IllegalArgumentException("name");

            productDB.setName(request.getName());
        }

        if(request.getDescription() != null) {
            productDB.setDescription(request.getDescription());
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
    public void removeProduct(long id, User currentUser) throws AccessDeniedException, ItemNotFoundException {
        if(currentUser == null)
            throw new AccessDeniedException(null);

        Product product = getProductByID(id);
        if (!currentUser.isAdmin() && currentUser.getId() != product.getOwnerId())
            throw new AccessDeniedException(null);

        productRepository.deleteById(id);
    }
}
