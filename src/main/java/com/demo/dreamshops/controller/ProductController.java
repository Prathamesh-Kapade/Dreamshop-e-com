package com.demo.dreamshops.controller;

import com.demo.dreamshops.dto.ProductDto;
import com.demo.dreamshops.exceptions.AlreadyExistsException;
import com.demo.dreamshops.model.Product;
import com.demo.dreamshops.request.AddProductRequest;
import com.demo.dreamshops.request.ProductUpdateRequest;
import com.demo.dreamshops.response.ApiResponse;
import com.demo.dreamshops.service.product.IProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/products")
@Slf4j
@Tag(name = "Product APIs")
public class ProductController {

    private final IProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category,
            Pageable pageable
    ) {
        log.info("Fetching products | name={}, brand={}, category={}", name, brand, category);

        try {
            Page<Product> productPage =
                    productService.getProducts(name, brand, category, pageable);

            List<ProductDto> products =
                    productService.getConvertedProducts(productPage.getContent());

            return ResponseEntity.ok(
                    new ApiResponse("Products fetched successfully", products)
            );

        } catch (Exception e) {
            log.error("Error fetching products", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("Failed to fetch products", null));
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable Long id) {
        log.info("Fetching product by ID: {}", id);

        try {
            Product product = productService.getProductById(id);
            ProductDto dto = productService.convertToDto(product);

            return ResponseEntity.ok(new ApiResponse("Success", dto));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse> addProduct(@RequestBody AddProductRequest request) {
        log.info("Adding product: {}", request.getName());

        try {
            Product product = productService.addProduct(request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("Product created successfully", product));

        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductUpdateRequest request
    ) {
        log.info("Updating product ID: {}", id);

        try {
            Product updatedProduct = productService.updateProduct(request, id);

            return ResponseEntity.ok(
                    new ApiResponse("Product updated successfully", updatedProduct)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long id) {
        log.info("Deleting product ID: {}", id);

        try {
            productService.deleteProductById(id);

            return ResponseEntity.ok(
                    new ApiResponse("Product deleted successfully", id)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }
}