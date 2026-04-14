package com.demo.dreamshops.service.product;

import com.demo.dreamshops.dto.ProductDto;
import com.demo.dreamshops.model.Product;
import com.demo.dreamshops.request.AddProductRequest;
import com.demo.dreamshops.request.ProductUpdateRequest;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IProductService {
    Product addProduct(AddProductRequest product);
    Product getProductById(Long id);
    void deleteProductById(Long id);
    Product updateProduct(ProductUpdateRequest product, Long productId);


    @Cacheable(value = "products", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    Page<Product> getAllProducts(Pageable pageable);

    List<Product> getProductByCategory(String category);
    List<Product> getProductByBrand(String brand);
    List<Product> getProductByCategoryAndBrand(String category, String brand);
    List<Product> getProductByName(String name);
    List<Product> getProductByBrandAndName(String category, String name);
    Long countProductsByBrandAndName(String brand, String name);

    List<ProductDto> getConvertedProducts(List<Product> products);

    ProductDto convertToDto(Product product);

    Page<Product> getProducts(String name, String brand, String category, Pageable pageable);
}
