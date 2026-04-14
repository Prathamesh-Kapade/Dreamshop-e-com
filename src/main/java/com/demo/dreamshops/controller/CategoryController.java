package com.demo.dreamshops.controller;

import com.demo.dreamshops.exceptions.AlreadyExistsException;
import com.demo.dreamshops.exceptions.ResourceNotFoundException;
import com.demo.dreamshops.model.Category;
import com.demo.dreamshops.request.CategoryRequest;
import com.demo.dreamshops.response.ApiResponse;
import com.demo.dreamshops.service.category.ICategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@RestController
@Tag(name="Category APIs")
@RequestMapping("${api.prefix}/categories")
@Slf4j
public class CategoryController {

    private final ICategoryService categoryService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllCategories(){
        log.info("Fetching all categories");
        try {
            List<Category> categories = categoryService.getAllCategories();
            log.info("Total categories found: {}", categories.size());
            return ResponseEntity.ok(new ApiResponse("Found", categories));
        } catch (Exception e) {
            log.error("Error while fetching categories", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Error", INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addCategory(@RequestBody CategoryRequest request) {

        log.info("Adding new category: {}", request.getName());

        try {
            Category category = new Category();
            category.setName(request.getName()); // ✅ only set name

            Category savedCategory = categoryService.addCategory(category);

            log.info("Category added successfully with ID: {}", savedCategory.getId());

            return ResponseEntity.ok(new ApiResponse("success", savedCategory));

        } catch (AlreadyExistsException e) {
            log.warn("Category already exists: {}", request.getName());
            return ResponseEntity.status(CONFLICT)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/category/id/{id}")
    public ResponseEntity<ApiResponse> getCategoryById(@PathVariable Long id){
        log.info("Fetching category by ID: {}", id);
        try {
            Category theCategory = categoryService.getCategoryById(id);
            return ResponseEntity.ok(new ApiResponse("Found",theCategory));
        } catch (ResourceNotFoundException e) {
            log.warn("Category not found with ID: {}", id);
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(),null));
        }
    }

    @GetMapping("/category/name/{name}")
    public ResponseEntity<ApiResponse> getCategoryByName(@PathVariable String name){
        log.info("Fetching category by name: {}", name);
        try {
            Category theCategory = categoryService.getCategoryByName(name);
            return ResponseEntity.ok(new ApiResponse("Found",theCategory));
        } catch (ResourceNotFoundException e) {
            log.warn("Category not found with name: {}", name);
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(),null));
        }
    }

    @DeleteMapping("/category/{id}/delete")
    public ResponseEntity<ApiResponse> deleteCategoryById(@PathVariable Long id) {

        log.info("Deleting category with ID: {}", id);

        try {
            categoryService.deleteCategoryById(id);
            log.info("Category deleted successfully: {}", id);

            return ResponseEntity.ok(new ApiResponse("Deleted", null));

        } catch (ResourceNotFoundException e) {

            log.warn("Category not found: {}", id);

            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));

        } catch (RuntimeException e) {  // 👈 ADD HERE

            log.warn("Delete failed due to linked products: {}", id);

            return ResponseEntity.status(CONFLICT)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/category/{id}/update")
    public ResponseEntity<ApiResponse> updateCategoryById(@PathVariable Long id,
                                                          @RequestBody Category category){
        log.info("Updating category with ID: {}", id);
        try {
            Category updatedCategory = categoryService.updateCategory(category,id);
            log.info("Category updated successfully: {}", id);
            return ResponseEntity.ok(new ApiResponse("Update success!",updatedCategory));
        } catch (ResourceNotFoundException e) {
            log.warn("Failed to update category with ID: {}", id);
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(),null));
        }
    }
}