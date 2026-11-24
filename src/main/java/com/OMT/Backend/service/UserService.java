package com.OMT.Backend.service;

import com.OMT.Backend.dto.UserCreateRequest;
import com.OMT.Backend.dto.UserDTO;
import com.OMT.Backend.dto.UserUpdateRequest;
import com.OMT.Backend.exception.BadRequestException;
import com.OMT.Backend.exception.ConflictException;
import com.OMT.Backend.exception.ResourceNotFoundException;
import com.OMT.Backend.model.User;
import com.OMT.Backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthService authService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       AuthService authService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(authService::toUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO createUser(UserCreateRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("A user account with email '" + email + "' already exists.");
        }

        String plainPassword;
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            authService.validatePasswordStrength(request.getPassword());
            plainPassword = request.getPassword();
        } else {
            plainPassword = generateRandomPassword();
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(plainPassword))
                .role(request.getRole())
                .canMakeOperations(request.isCanMakeOperations())
                .active(true)
                .build();

        User saved = userRepository.save(user);

        // Send email with credentials to the newly created staff member!
        emailService.sendWelcomeCredentialsEmail(
                saved.getEmail(),
                saved.getName(),
                plainPassword,
                saved.getRole().name(),
                saved.isCanMakeOperations()
        );

        return authService.toUserDTO(saved);
    }

    @Transactional
    public UserDTO updateUserPermissions(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if (request.getCanMakeOperations() != null) {
            user.setCanMakeOperations(request.getCanMakeOperations());
        }

        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        User updated = userRepository.save(user);
        return authService.toUserDTO(updated);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Soft deactivate or delete
        userRepository.delete(user);
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789@#$";
        SecureRandom sr = new SecureRandom();
        StringBuilder sb = new StringBuilder("Omt@");
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(sr.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
