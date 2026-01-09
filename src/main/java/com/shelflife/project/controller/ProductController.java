package com.shelflife.project.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.shelflife.project.dto.CreateProductRequest;
import com.shelflife.project.dto.UpdateProductRequest;
import com.shelflife.project.exception.BarcodeExistsException;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Image;
import com.shelflife.project.model.Product;
import com.shelflife.project.service.ImageService;
import com.shelflife.project.service.ProductService;

import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ImageService imageService;

    @GetMapping()
    public List<Product> getProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable long id) {
        try {
            return ResponseEntity.ok(productService.getProductByID(id));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/icon")
    public ResponseEntity<Resource> getIcon(@PathVariable long id) {
        String filename = id + "_productIcon";
        Resource resource = imageService.loadImage(filename, "classpath:product-svgrepo-com.svg");

        try {
            Image image = imageService.getImage(filename);

            if (!resource.getFilename().equals(filename))
                throw new ItemNotFoundException("image", "Image file was not found");

            return ResponseEntity.ok().header("Content-Type", image.getMimetype()).body(resource);

        } catch (ItemNotFoundException e) {
            return ResponseEntity.ok().header("Content-Type", "image/svg+xml").body(resource);
        }
    }

    @PostMapping("/{id}/icon")
    public ResponseEntity<?> uploadIcon(@PathVariable long id, @RequestParam("pfp") MultipartFile file,
            Authentication auth) {
        try {
            if (!productService.canEditProduct(id, auth))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

            imageService.uploadImage(file, id + "_productIcon");
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        } catch (InvalidMimeTypeException e) {
            return ResponseEntity.badRequest().body(Map.of("pfp", "Invalid mime type"));
        }
    }

    @GetMapping("/barcode/{code}")
    public ResponseEntity<?> getProductByBarcode(@PathVariable String code) {
        try {
            return ResponseEntity.ok(productService.getProductByBarcode(code));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping()
    public ResponseEntity<?> createProduct(@Valid @RequestBody CreateProductRequest request, Authentication auth) {
        try {
            return ResponseEntity.ok(productService.saveProduct(request, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (BarcodeExistsException e) {
            return ResponseEntity.badRequest().body(Map.of("barcode", "This barcode already exists"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(e.getMessage(), "Invalid value"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable long id, Authentication auth) {
        try {
            productService.removeProduct(id, auth);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable long id, @Valid @RequestBody UpdateProductRequest request,
            Authentication auth) {
        try {
            return ResponseEntity.ok(productService.updateProduct(id, request, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (BarcodeExistsException e) {
            return ResponseEntity.badRequest().body(Map.of("barcode", "This barcode already exists"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(e.getMessage(), "Invalid value"));
        }
    }
}
