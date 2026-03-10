package com.shelflife.project.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.shelflife.project.dto.product.CreateProductRequest;
import com.shelflife.project.dto.product.UpdateProductRequest;
import com.shelflife.project.exception.BarcodeExistsException;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Image;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.User;
import com.shelflife.project.service.ImageService;
import com.shelflife.project.service.ProductService;
import com.shelflife.project.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private UserService userService;

    @Autowired
    private ImageService imageService;

    @Operation(summary = "Get all products", description = "Retrieve a list of all products based on optional filters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved products")
    })
    @GetMapping()
    public List<Product> getProducts(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending) {

        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable;

        if (size == null) {
            pageable = Pageable.unpaged();
        } else {
            pageable = PageRequest.of(page, size, sort);
        }

        return productService.findProducts(search, pageable);
    }

    @Operation(summary = "Get product by ID", description = "Retrieve a product by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved product"),
            @ApiResponse(responseCode = "404", description = "Product not found", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable long id) {
        try {
            return ResponseEntity.ok(productService.getProductByID(id));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Gets categories that are in the database")
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(productService.getCategories());
    }

    @GetMapping("/{id}/icon")
    @Operation(summary = "Get the icon of a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "image/*")
            }, description = "Returns an image with it's own mime type header. If there is no uploaded image, a placeholder svg will be returned"),
    })
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
    @Operation(summary = "Upload an icon for a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "500", description = "An IO Exception occured", content = {
                    @Content(schema = @Schema(example = "{ \"error\": \"Couldn't upload image\" }"))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid mime type", content = {
                    @Content(schema = @Schema(example = "{ \"pfp\": \"Invalid mime type\" }"))
            }),
    })
    public ResponseEntity<Map<String, String>> uploadIcon(@PathVariable long id,
            @RequestParam("pfp") MultipartFile file,
            Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);

            if (!productService.canEditProduct(id, user))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

            imageService.uploadImage(file, id + "_productIcon");
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Couldn't upload image"));
        } catch (InvalidMimeTypeException e) {
            return ResponseEntity.badRequest().body(Map.of("pfp", "Invalid mime type"));
        }
    }

    @Operation(summary = "Create a new product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product created successfully", content = {
                    @Content(schema = @Schema(implementation = Product.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid input or barcode already exists", content = {
                    @Content(schema = @Schema(example = "{ \"barcode\": \"Barcode already exists\" }"))
            }),
            @ApiResponse(responseCode = "403", description = "Access denied", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    @PostMapping()
    public ResponseEntity<?> createProduct(@Valid @RequestBody CreateProductRequest request, Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity.ok(productService.saveProduct(request, user));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (BarcodeExistsException e) {
            return ResponseEntity.badRequest().body(Map.of("barcode", "This barcode already exists"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(e.getMessage(), "Invalid value"));
        }
    }

    @Operation(summary = "Delete a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable long id, Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            productService.removeProduct(id, user);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Update a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully", content = {
                    @Content(schema = @Schema(implementation = Product.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid input or barcode already exists", content = {
                    @Content(schema = @Schema(example = "{ \"barcode\": \"Barcode already exists\" }"))
            }),
            @ApiResponse(responseCode = "403", description = "Access denied", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "404", description = "Product not found", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable long id, @Valid @RequestBody UpdateProductRequest request,
            Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity.ok(productService.updateProduct(id, request, user));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (BarcodeExistsException e) {
            return ResponseEntity.badRequest().body(Map.of("barcode", "This barcode already exists"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(e.getMessage(), "Invalid value"));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(e.getMessage(), "Invalid value"));
        }
    }
}
