package com.demo.dreamshops.service.product;

import com.demo.dreamshops.dto.ImageDto;
import com.demo.dreamshops.dto.ProductDto;
import com.demo.dreamshops.exceptions.AlreadyExistsException;
import com.demo.dreamshops.exceptions.ProductNotFoundException;
import com.demo.dreamshops.model.Category;
import com.demo.dreamshops.model.Product;
import com.demo.dreamshops.repository.CategoryRepository;
import com.demo.dreamshops.repository.ImageRepository;
import com.demo.dreamshops.repository.ProductRepository;
import com.demo.dreamshops.request.AddProductRequest;
import com.demo.dreamshops.request.ProductUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final ImageRepository imageRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    @CacheEvict(value = {"products", "productsByCategory"}, allEntries = true)
    public Product addProduct(AddProductRequest request) {
        log.info("SERVICE CALL: Add product with name={} and brand={}", request.getName(), request.getBrand());

        if (productExists(request.getName(), request.getBrand())) {
            log.warn("Product already exists: brand={} name={}", request.getBrand(), request.getName());
            throw new AlreadyExistsException(request.getBrand() + " " + request.getName() + " already exists, update instead!");
        }

        Category category = Optional.ofNullable(categoryRepository.findByName(request.getCategory().getName()))
                .orElseGet(() -> {
                    log.info("Category not found, creating new category={}", request.getCategory().getName());
                    Category newCategory = new Category(request.getCategory().getName());
                    return categoryRepository.save(newCategory);
                });

        request.setCategory(category);
        Product product = productRepository.save(createProduct(request, category));
        log.info("Product added successfully with id={}", product.getId());
        return product;
    }

    private boolean productExists(String name, String brand) {
        boolean exists = productRepository.existsByNameAndBrand(name, brand);
        log.debug("Check product exists: name={} brand={} result={}", name, brand, exists);
        return exists;
    }

    private Product createProduct(AddProductRequest request, Category category) {
        log.debug("Creating product entity for name={} brand={}", request.getName(), request.getBrand());
        return new Product(
                request.getName(),
                request.getBrand(),
                request.getPrice(),
                request.getInventory(),
                request.getDescription(),
                category
        );
    }

    @Override
    @Cacheable(value = "product", key = "#id")
    public Product getProductById(Long id) {
        log.info("SERVICE CALL: Get product by id={}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found for id={}", id);
                    return new ProductNotFoundException("Product not found!");
                });
    }

    @Override
    @CacheEvict(value = {"products", "product", "productsByCategory"}, allEntries = true)
    public void deleteProductById(Long id) {
        log.info("SERVICE CALL: Delete product by id={}", id);
        productRepository.findById(id)
                .ifPresentOrElse(product -> {
                            productRepository.delete(product);
                            log.info("Product deleted successfully id={}", id);
                        },
                        () -> {
                            log.warn("Product not found for deletion id={}", id);
                            throw new ProductNotFoundException("Product not found!");
                        });
    }

    @Override
    @CacheEvict(value = {"products", "product", "productsByCategory"}, allEntries = true)
    public Product updateProduct(ProductUpdateRequest request, Long productId) {
        log.info("SERVICE CALL: Update product id={}", productId);
        return productRepository.findById(productId)
                .map(existingProduct -> updateExistingProduct(existingProduct, request))
                .map(product -> {
                    log.info("Product updated successfully id={}", productId);
                    return productRepository.save(product);
                })
                .orElseThrow(() -> {
                    log.warn("Product not found for update id={}", productId);
                    return new ProductNotFoundException("Product not found!");
                });
    }


    private Product updateExistingProduct(Product existingProduct, ProductUpdateRequest request) {
        log.debug("Updating product fields for id={}", existingProduct.getId());
        existingProduct.setName(request.getName());
        existingProduct.setBrand(request.getBrand());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setInventory(request.getInventory());
        existingProduct.setDescription(request.getDescription());

        Category category = categoryRepository.findByName(request.getCategory().getName());
        existingProduct.setCategory(category);
        return existingProduct;
    }

    @Cacheable(value = "products", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @Override
    public Page<Product> getAllProducts(Pageable pageable) {

        log.info("SERVICE CALL: Get products page {} with size {}",
                pageable.getPageNumber(),
                pageable.getPageSize());

        Page<Product> products = productRepository.findAll(pageable);

        log.info("Fetched {} products from DB", products.getNumberOfElements());

        return products;
    }

    @Override
    @Cacheable(value = "productsByCategory", key = "#category")
    public List<Product> getProductByCategory(String category) {
        log.info("SERVICE CALL: Get products by category={}", category);
        List<Product> products = productRepository.findByCategoryNameIgnoreCase(category);
        log.info("Fetched {} products for category={}", products.size(), category);
        return products;
    }

    @Override
    public List<Product> getProductByBrand(String brand) {
        log.info("SERVICE CALL: Get products by brand={}", brand);
        List<Product> products = productRepository.findByBrand(brand);
        log.info("Fetched {} products for brand={}", products.size(), brand);
        return products;
    }

    @Override
    public List<Product> getProductByCategoryAndBrand(String category, String brand) {
        log.info("SERVICE CALL: Get products by category={} and brand={}", category, brand);
        List<Product> products = productRepository.findByCategoryNameAndBrand(category, brand);
        log.info("Fetched {} products for category={} and brand={}", products.size(), category, brand);
        return products;
    }

    @Override
    public List<Product> getProductByName(String name) {
        log.info("SERVICE CALL: Get products by name={}", name);
        List<Product> products = productRepository.findByName(name);
        log.info("Fetched {} products for name={}", products.size(), name);
        return products;
    }

    @Override
    public List<Product> getProductByBrandAndName(String brand, String name) {
        log.info("SERVICE CALL: Get products by brand={} and name={}", brand, name);
        List<Product> products = productRepository.findByBrandAndName(brand, name);
        log.info("Fetched {} products for brand={} and name={}", products.size(), brand, name);
        return products;
    }

    @Override
    public Long countProductsByBrandAndName(String brand, String name) {
        log.info("SERVICE CALL: Count products by brand={} and name={}", brand, name);
        Long count = productRepository.countByBrandAndName(brand, name);
        log.info("Counted {} products for brand={} and name={}", count, brand, name);
        return count;
    }

    @Override
    public List<ProductDto> getConvertedProducts(List<Product> products) {
        log.debug("Converting {} products to DTOs", products.size());
        return products.stream().map(this::convertToDto).toList();
    }

    @Override
    public ProductDto convertToDto(Product product) {
        log.debug("Converting product id={} to DTO", product.getId());
        ProductDto dto = modelMapper.map(product, ProductDto.class);

        List<ImageDto> images = imageRepository
                .findByProductId(product.getId())
                .stream()
                .map(image -> {
                    ImageDto imageDto = new ImageDto();
                    imageDto.setId(image.getId());
                    imageDto.setFileName(image.getFileName());
                    return imageDto;
                }).toList();

        dto.setImages(images);
        log.debug("Product id={} converted to DTO with {} images", product.getId(), images.size());
        return dto;
    }

    @Override
    public Page<Product> getProducts(String name, String brand, String category, Pageable pageable) {

        if (name != null) {
            return productRepository.findByNameContainingIgnoreCase(name, pageable);
        }

        if (brand != null) {
            return productRepository.findByBrandContainingIgnoreCase(brand, pageable);
        }

        if (category != null) {
            return productRepository.findByCategoryContainingIgnoreCase(category, pageable);
        }

        return productRepository.findAll(pageable);
    }
}