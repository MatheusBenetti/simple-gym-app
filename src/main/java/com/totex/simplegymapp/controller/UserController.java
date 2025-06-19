package com.totex.simplegymapp.controller;

import com.totex.simplegymapp.business.dto.UserCreateDto;
import com.totex.simplegymapp.business.dto.UserPasswordUpdateDto;
import com.totex.simplegymapp.business.dto.UserResponseDto;
import com.totex.simplegymapp.business.dto.UserUpdateDto;
import com.totex.simplegymapp.business.service.UserService;
import com.totex.simplegymapp.infrastructure.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody @Valid UserCreateDto userCreateDto) {
        return ResponseEntity.ok(userService.createUser(userCreateDto));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserCreateDto userCreateDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userCreateDto.getEmail(), userCreateDto.getPassword())
        );

        String token = jwtUtil.generateToken(authentication.getName());

        userService.cacheUserToken(userCreateDto.getEmail(), token);

        return ResponseEntity.ok(Map.of(
                "token", "Bearer " + token,
                "type", "Bearer",
                "email", authentication.getName()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractEmailToken(token.substring(7));

        userService.invalidateUserToken(email);

        userService.clearUserSession(email);

        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7);
            String email = jwtUtil.extractEmailToken(jwtToken);

            boolean isTokenCached = userService.isTokenCached(email, jwtToken);
            boolean isTokenValid = jwtUtil.validateToken(jwtToken, email);

            if (isTokenValid && isTokenCached) {
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "email", email,
                        "message", "Token is valid"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Token is invalid or expired"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "Invalid token format"
            ));
        }
    }

    @GetMapping()
    public ResponseEntity<UserResponseDto> getUserByEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(userService.findUserByEmail(email));
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteByEmail(@PathVariable String email) {
        userService.deleteUserByEmail(email);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<UserResponseDto> updateUser(
            @RequestBody @Valid UserUpdateDto userDto,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userService.updateUser(token, userDto));
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> updatePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid UserPasswordUpdateDto dto) {
        userService.updateUserPassword(token, dto);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }
}