package com.demo.dreamshops.service.category;

import com.demo.dreamshops.exceptions.AlreadyExistsException;
import com.demo.dreamshops.exceptions.ResourceNotFoundException;
import com.demo.dreamshops.model.Category;
import com.demo.dreamshops.model.Product;
import com.demo.dreamshops.repository.CategoryRepository;
import com.demo.dreamshops.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Cacheable(value = "category", key = "#id")
    public Category getCategoryById(Long id) {
        log.info("Fetching category by ID: {}", id);

        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category not found with ID: {}", id);
                    return new ResourceNotFoundException("Category not found!");
                });
    }

    @Override
    @Cacheable(value = "categoryByName", key = "#name")
    public Category getCategoryByName(String name) {
        log.info("Fetching category by name: {}", name);

        Category category = categoryRepository.findByName(name);

        if (category == null) {
            log.warn("Category not found with name: {}", name);
        } else {
            log.info("Category found with ID: {}", category.getId());
        }

        return category;
    }

    @Override
    @Cacheable(value = "categories")
    public List<Category> getAllCategories() {
        log.info("Fetching all categories");

        List<Category> categories = categoryRepository.findAll();

        log.info("Total categories found: {}", categories.size());
        return categories;
    }

    @Override
    @CacheEvict(value = {"categories", "category", "categoryByName"}, allEntries = true)
    public Category addCategory(Category category) {

        Optional<Category> existing = Optional.ofNullable(categoryRepository.findByName(category.getName()));

        if (existing.isPresent()) {
            throw new AlreadyExistsException("Category already exists!");
        }

        category.setId(null);

        return categoryRepository.save(category);
    }

    @Override
    @CacheEvict(value = {"categories", "category", "categoryByName"}, allEntries = true)
    public Category updateCategory(Category category, Long id) {
        log.info("Updating category | id: {}, newName: {}", id, category.getName());

        return Optional.ofNullable(getCategoryById(id))
                .map(oldCategory -> {
                    oldCategory.setName(category.getName());
                    Category updated = categoryRepository.save(oldCategory);

                    log.info("Category updated successfully | id: {}", id);
                    return updated;
                })
                .orElseThrow(() -> {
                    log.warn("Category not found for update | id: {}", id);
                    return new ResourceNotFoundException("Category not found!");
                });
    }

    @Override
    @CacheEvict(value = {"categories", "category", "categoryByName"}, allEntries = true)
    public void deleteCategoryById(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        List<Product> products = productRepository.findByCategoryId(id);

        if (!products.isEmpty()) {
            throw new RuntimeException("Cannot delete category. Products are linked to it!");
        }

        categoryRepository.delete(category);
    }
}