package com.mediscan.auth.controller;

import com.mediscan.dto.v1.auth.AuthResponseDTO;
import com.mediscan.dto.v1.auth.LoginRequestDTO;
import com.mediscan.dto.v1.auth.RegisterRequestDTO;
import com.mediscan.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mediscan.dto.v1.auth.TokenRefreshRequestDTO;
import com.mediscan.auth.entity.RefreshToken;
import com.mediscan.auth.service.RefreshTokenService;
import com.mediscan.auth.security.JwtUtil;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody @Valid RegisterRequestDTO request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refreshToken(@RequestBody @Valid TokenRefreshRequestDTO request) {
        return refreshTokenService.findByToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtUtil.generateToken(
                            new org.springframework.security.core.userdetails.User(
                                    user.getUsername(),
                                    user.getPasswordHash(),
                                    java.util.Collections.singletonList(
                                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                                    "ROLE_" + user.getRole().name()))));

                    return ResponseEntity.ok(AuthResponseDTO.builder()
                            .token(accessToken)
                            .refreshToken(request.getRefreshToken())
                            .build());
                })
                .orElseThrow(() -> new com.mediscan.common.exception.UnauthorizedAccessException(
                        "Refresh token is not in database!"));
    }
}
