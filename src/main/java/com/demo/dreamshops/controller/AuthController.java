package com.demo.dreamshops.controller;

import com.demo.dreamshops.data.RoleRepository;
import com.demo.dreamshops.model.Role;
import com.demo.dreamshops.model.User;
import com.demo.dreamshops.repository.UserRepository;
import com.demo.dreamshops.request.LoginRequest;
import com.demo.dreamshops.request.RegisterRequest;
import com.demo.dreamshops.response.ApiResponse;
import com.demo.dreamshops.response.JwtResponse;
import com.demo.dreamshops.security.jwt.JwtBlacklistService;
import com.demo.dreamshops.security.jwt.JwtUtils;
import com.demo.dreamshops.security.user.ShopUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final JwtBlacklistService jwtBlacklistService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;


    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse("Email already registered", null));
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.getRoles().add(userRole);

        userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse("Registration successful", null));
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request){

        log.info("Login attempt for email: {}", request.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtils.generateTokenForUser(authentication);


            ShopUserDetails userDetails = (ShopUserDetails) authentication.getPrincipal();

            JwtResponse jwtResponse = new JwtResponse(userDetails.getId(), jwt);

            log.info("Login successful for email: {}", request.getEmail());
            return ResponseEntity.ok(new ApiResponse("Login Successful", jwtResponse));

        } catch (AuthenticationException e) {
            log.warn("Login failed for email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("Invalid email or password", null));
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            long expiration = jwtUtils.getRemainingExpiration(token);
            jwtBlacklistService.blacklistToken(token, expiration);
            log.info("Token blacklisted successfully");
            return ResponseEntity.ok("Logged out successfully");
        }

        return ResponseEntity.badRequest().body("Invalid token");
    }

    @lombok.Data
    public static class RegisterRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String password;
    }
}