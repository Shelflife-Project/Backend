package com.shelflife.project.docs;

import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.shelflife.project.dto.product.CreateProductRequest;
import com.shelflife.project.dto.product.UpdateProductRequest;
import com.shelflife.project.model.Product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface ProductControllerDocs {
    @Operation(summary = "Get all products", description = "Retrieve a list of all products based on optional filters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved products")
    })
    public List<Product> getProducts(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending);

    @Operation(summary = "Get product by ID", description = "Retrieve a product by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved product"),
            @ApiResponse(responseCode = "404", description = "Product not found", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<Product> getProduct(long id);

    @Operation(summary = "Gets categories that are in the database")
    public ResponseEntity<List<String>> getCategories();

    @Operation(summary = "Get the icon of a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "image/*")
            }, description = "Returns an image with it's own mime type header. If there is no uploaded image, a placeholder svg will be returned"),
    })
    public ResponseEntity<Resource> getIcon(long id);

    @Operation(summary = "Get optimized 64x64 icon of a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "image/*")
            }, description = "Returns an optimized image (max 64x64). If there is no uploaded image, a placeholder will be returned"),
    })
    public ResponseEntity<byte[]> getSmallIcon(long id);

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
    public ResponseEntity<Map<String, String>> uploadIcon(long id,
            MultipartFile file,
            Authentication auth);

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
    public ResponseEntity<?> createProduct(CreateProductRequest request, Authentication auth);

    @Operation(summary = "Delete a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> deleteProduct(long id, Authentication auth);

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
    public ResponseEntity<?> updateProduct(long id, UpdateProductRequest request, Authentication auth);
}
