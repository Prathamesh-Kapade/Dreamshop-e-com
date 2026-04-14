package com.demo.dreamshops.controller;

import com.demo.dreamshops.dto.UserDto;
import com.demo.dreamshops.exceptions.AlreadyExistsException;
import com.demo.dreamshops.exceptions.ResourceNotFoundException;
import com.demo.dreamshops.model.User;
import com.demo.dreamshops.request.CreateUserRequest;
import com.demo.dreamshops.request.UserUpdateRequest;
import com.demo.dreamshops.response.ApiResponse;
import com.demo.dreamshops.service.user.IUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name="User APIs")
@RequestMapping("${api.prefix}/users")
public class UserController {
    private final IUserService userService;

    @GetMapping("/{userId}/user")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long userId){
        log.info("API CALL: Get user by userId={}", userId);
        try {
            User user = userService.getUserById(userId);
            log.info("User fetched successfully for userId={}", userId);
            return ResponseEntity.ok(new ApiResponse("Success", user));
        } catch (ResourceNotFoundException e) {
            log.warn("User not found for userId={}", userId);
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(),null));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> createUser(@RequestBody CreateUserRequest request){
        log.info("API CALL: Create user with email={}", request.getEmail());
        try {
            User user= userService.createUser(request);
            UserDto userDto = userService.convertUserToDto(user);
            log.info("User created successfully with id={}", user.getId());
            return ResponseEntity.ok(new ApiResponse("Create User Success!",userDto));
        } catch (AlreadyExistsException e) {
            log.warn("User already exists with email={}", request.getEmail());
            return ResponseEntity.status(CONFLICT).body(new ApiResponse(e.getMessage(),null));
        } catch (Exception e) {
            log.error("Error while creating user with email={}", request.getEmail(), e);
            return ResponseEntity.status(CONFLICT).body(new ApiResponse("Error Occurred", e.getMessage()));
        }
    }

    @PutMapping("/{userId}/update")
    public ResponseEntity<ApiResponse> updateUser(@RequestBody UserUpdateRequest request,@PathVariable Long userId ){
        log.info("API CALL: Update user with userId={}", userId);
        try {
            User userDto= userService.updateUser(request, userId);
            log.info("User updated successfully for userId={}", userId);
            return ResponseEntity.ok(new ApiResponse("Update User Success!", userDto));
        } catch (ResourceNotFoundException e) {
            log.warn("User not found for update, userId={}", userId);
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(),null));
        } catch (Exception e) {
            log.error("Error while updating user with userId={}", userId, e);
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Error Occurred", e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}/delete")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long userId ){
        log.info("API CALL: Delete user with userId={}", userId);
        try {
            userService.deleteUser(userId);
            log.info("User deleted successfully for userId={}", userId);
            return ResponseEntity.ok(new ApiResponse("Delete User Success!",null));
        } catch (ResourceNotFoundException e) {
            log.warn("User not found for deletion, userId={}", userId);
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(),null));
        } catch (Exception e) {
            log.error("Error while deleting user with userId={}", userId, e);
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Error Occurred", e.getMessage()));
        }
    }
}