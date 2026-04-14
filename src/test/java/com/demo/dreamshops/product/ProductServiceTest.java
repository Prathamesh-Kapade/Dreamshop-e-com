package com.demo.dreamshops.product;

import com.demo.dreamshops.exceptions.AlreadyExistsException;
import com.demo.dreamshops.exceptions.ProductNotFoundException;
import com.demo.dreamshops.model.Category;
import com.demo.dreamshops.model.Product;
import com.demo.dreamshops.repository.CategoryRepository;
import com.demo.dreamshops.repository.ImageRepository;
import com.demo.dreamshops.repository.ProductRepository;
import com.demo.dreamshops.request.AddProductRequest;
import com.demo.dreamshops.request.ProductUpdateRequest;
import com.demo.dreamshops.service.product.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProductService productService;

    // ===============================
    // ✅ TEST: addProduct SUCCESS
    // ===============================
    @Test
    void testAddProduct_Success() {

        AddProductRequest request = new AddProductRequest();
        request.setName("Laptop");
        request.setBrand("Dell");
        request.setPrice(new BigDecimal("50000"));
        request.setInventory(10);
        request.setDescription("Good laptop");

        Category category = new Category("Electronics");
        request.setCategory(category);

        when(productRepository.existsByNameAndBrand("Laptop", "Dell"))
                .thenReturn(false);

        when(categoryRepository.findByName("Electronics"))
                .thenReturn(category);

        when(productRepository.save(any(Product.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        Product product = productService.addProduct(request);

        assertNotNull(product);
        assertEquals("Laptop", product.getName());

        verify(productRepository).save(any(Product.class));
    }

    // ===============================
    // ❌ TEST: addProduct ALREADY EXISTS
    // ===============================
    @Test
    void testAddProduct_AlreadyExists() {

        AddProductRequest request = new AddProductRequest();
        request.setName("Laptop");
        request.setBrand("Dell");

        when(productRepository.existsByNameAndBrand("Laptop", "Dell"))
                .thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> {
            productService.addProduct(request);
        });
    }

    // ===============================
    // ✅ TEST: getProductById SUCCESS
    // ===============================
    @Test
    void testGetProductById_Success() {

        Product product = new Product();
        product.setId(1L);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        Product result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    // ===============================
    // ❌ TEST: getProductById NOT FOUND
    // ===============================
    @Test
    void testGetProductById_NotFound() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> {
            productService.getProductById(1L);
        });
    }

    // ===============================
    // ✅ TEST: deleteProduct SUCCESS
    // ===============================
    @Test
    void testDeleteProduct_Success() {

        Product product = new Product();
        product.setId(1L);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        productService.deleteProductById(1L);

        verify(productRepository).delete(product);
    }

    // ===============================
    // ❌ TEST: deleteProduct NOT FOUND
    // ===============================
    @Test
    void testDeleteProduct_NotFound() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> {
            productService.deleteProductById(1L);
        });
    }

    // ===============================
    // ✅ TEST: updateProduct SUCCESS
    // ===============================
    @Test
    void testUpdateProduct_Success() {

        Product existingProduct = new Product();
        existingProduct.setId(1L);

        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("Updated Laptop");
        request.setBrand("HP");

        Category category = new Category("Electronics");
        request.setCategory(category);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(existingProduct));

        when(categoryRepository.findByName("Electronics"))
                .thenReturn(category);

        when(productRepository.save(any(Product.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        Product updated = productService.updateProduct(request, 1L);

        assertEquals("Updated Laptop", updated.getName());
    }

    // ===============================
    // ❌ TEST: updateProduct NOT FOUND
    // ===============================
    @Test
    void testUpdateProduct_NotFound() {

        ProductUpdateRequest request = new ProductUpdateRequest();

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> {
            productService.updateProduct(request, 1L);
        });
    }

    // ===============================
    // ✅ TEST: getAllProducts
    // ===============================
    @Test
    void testGetAllProducts() {

        Pageable pageable = PageRequest.of(0, 2);

        Page<Product> page = new PageImpl<>(List.of(new Product(), new Product()));

        when(productRepository.findAll(pageable)).thenReturn(page);

        Page<Product> result = productService.getAllProducts(pageable);

        assertEquals(2, result.getNumberOfElements());
    }
}