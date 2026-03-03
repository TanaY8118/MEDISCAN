package com.mediscan.auth.service;

import com.mediscan.dto.v1.auth.AuthResponseDTO;
import com.mediscan.dto.v1.auth.LoginRequestDTO;
import com.mediscan.dto.v1.auth.RegisterRequestDTO;
import com.mediscan.auth.security.JwtUtil;
import com.mediscan.user.entity.User;
import com.mediscan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;
        private final AuthenticationManager authenticationManager;
        private final RefreshTokenService refreshTokenService;

        public AuthResponseDTO register(RegisterRequestDTO request) {
                if (userRepository.existsByUsername(request.getUsername())
                                || userRepository.existsByEmail(request.getEmail())) {
                        throw new com.mediscan.common.exception.ResourceConflictException(
                                        "Username or Email already exists");
                }

                var user = User.builder()
                                .username(request.getUsername())
                                .email(request.getEmail())
                                .passwordHash(passwordEncoder.encode(request.getPassword()))
                                .role(User.Role.USER) // Default role
                                .build();

                userRepository.save(user);

                // Auto-login after register OR return simple success message.
                // Returning token here for seamless UX.
                // We need to reload user from DB to be safe or just use the obj.
                // But loadUserByUsername logic is needed for UserDetails.
                // Simply reusing the jwt generation logic which needs UserDetails.
                // Let's create a UserDetails object manually or fetch it.

                // Actually slightly cleaner to just return the token.
                // But UserDetails is required by JwtUtil.generateToken.
                // A simple workaround is calling the same loadUserByUsername logic
                // OR adhering to the contract.
                // Since we satisfy Role.USER, let's just use the UserService logic.
                // But UserService is in another package.
                // Let's just Authenticate to be safe.

                var jwtToken = jwtUtil.generateToken(
                                new org.springframework.security.core.userdetails.User(
                                                user.getUsername(),
                                                user.getPasswordHash(),
                                                java.util.Collections.singletonList(
                                                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                                                                "ROLE_" + user.getRole().name()))));

                var refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

                return AuthResponseDTO.builder()
                                .token(jwtToken)
                                .refreshToken(refreshToken.getToken())
                                .build();
        }

        public AuthResponseDTO login(LoginRequestDTO request) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getUsername(),
                                                request.getPassword()));
                var user = userRepository.findByUsername(request.getUsername())
                                .orElseThrow();
                var jwtToken = jwtUtil.generateToken(
                                new org.springframework.security.core.userdetails.User(
                                                user.getUsername(),
                                                user.getPasswordHash(),
                                                java.util.Collections.singletonList(
                                                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                                                                "ROLE_" + user.getRole().name()))));

                var refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

                return AuthResponseDTO.builder()
                                .token(jwtToken)
                                .refreshToken(refreshToken.getToken())
                                .build();
        }
}
