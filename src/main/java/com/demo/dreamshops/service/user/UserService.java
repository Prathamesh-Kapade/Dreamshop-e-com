package com.demo.dreamshops.service.user;

import com.demo.dreamshops.data.RoleRepository;
import com.demo.dreamshops.dto.UserDto;
import com.demo.dreamshops.exceptions.AlreadyExistsException;
import com.demo.dreamshops.exceptions.ResourceNotFoundException;
import com.demo.dreamshops.model.User;
import com.demo.dreamshops.repository.UserRepository;
import com.demo.dreamshops.request.CreateUserRequest;
import com.demo.dreamshops.request.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public User getUserById(Long userId) {
        log.info("SERVICE CALL: Get user by id={}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for id={}", userId);
                    return new ResourceNotFoundException("User not found!");
                });
    }

    @Override
    public User createUser(CreateUserRequest request) {
        log.info("SERVICE CALL: Create user with email={}", request.getEmail());
        return Optional.of(request)
                .filter(user -> !userRepository.existsByEmail(request.getEmail()))
                .map(req -> {
                    User user = new User();
                    user.setEmail(request.getEmail());
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    user.setFirstName(request.getFirstName());
                    user.setLastName(request.getLastName());
                    User savedUser = userRepository.save(user);
                    log.info("User created successfully with id={}", savedUser.getId());
                    return savedUser;
                }).orElseThrow(() -> {
                    log.warn("User already exists with email={}", request.getEmail());
                    return new AlreadyExistsException("Oops! " + request.getEmail() + " already exists!");
                });
    }

    @Override
    public User updateUser(UserUpdateRequest request, Long userId) {
        log.info("SERVICE CALL: Update user with id={}", userId);
        return userRepository.findById(userId).map(existingUser -> {
            existingUser.setFirstName(request.getFirstName());
            existingUser.setLastName(request.getLastName());
            User updatedUser = userRepository.save(existingUser);
            log.info("User updated successfully id={}", userId);
            return updatedUser;
        }).orElseThrow(() -> {
            log.warn("User not found for update id={}", userId);
            return new ResourceNotFoundException("User not found!");
        });
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("SERVICE CALL: Delete user with id={}", userId);
        userRepository.findById(userId).ifPresentOrElse(user -> {
            userRepository.delete(user);
            log.info("User deleted successfully id={}", userId);
        }, () -> {
            log.warn("User not found for deletion id={}", userId);
            throw new ResourceNotFoundException("User not found!");
        });
    }

    @Override
    public UserDto convertUserToDto(User user) {
        log.debug("Converting user id={} to DTO", user.getId());
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public User getAuthenticatedUser() {
        log.info("SERVICE CALL: Get authenticated user");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.debug("Authenticated user email={}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        if (user == null) {
            log.warn("Authenticated user not found for email={}", email);
            throw new ResourceNotFoundException("Authenticated user not found!");
        }
        log.info("Authenticated user fetched successfully id={}", user.getId());
        return user;
    }
}
