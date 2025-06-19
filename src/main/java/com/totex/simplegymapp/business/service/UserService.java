package com.totex.simplegymapp.business.service;

import com.totex.simplegymapp.business.converter.UserConverter;
import com.totex.simplegymapp.business.dto.UserCreateDto;
import com.totex.simplegymapp.business.dto.UserPasswordUpdateDto;
import com.totex.simplegymapp.business.dto.UserResponseDto;
import com.totex.simplegymapp.business.dto.UserUpdateDto;
import com.totex.simplegymapp.infrastructure.exception.ConflictException;
import com.totex.simplegymapp.infrastructure.exception.ResourceNotFoundException;
import com.totex.simplegymapp.infrastructure.model.UserModel;
import com.totex.simplegymapp.infrastructure.repository.UserRepository;
import com.totex.simplegymapp.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    public UserResponseDto createUser(UserCreateDto userDto) {
        emailExists(userDto.getEmail());

        UserModel user = userConverter.toUserModel(userDto);
        user = userRepository.save(user);

        return userConverter.toUserResponseDto(user);
    }

    public void emailExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists.");
        }
    }

    @Cacheable(value = "users", key = "#email")
    public UserResponseDto findUserByEmail(String email) {
        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found."));
        return userConverter.toUserResponseDto(user);
    }

    @CacheEvict(value = "users", key = "#email")
    public void deleteUserByEmail(String email) {
        redisTemplate.delete("jwt-token:" + email);
        userRepository.deleteByEmail(email);
    }

    @CachePut(value = "users", key = "#result.email")
    public UserResponseDto updateUser(String token, UserUpdateDto userDto) {
        String email = jwtUtil.extractEmailToken(token.substring(7));

        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found."));

        userConverter.updateUserModel(user, userDto);

        UserModel savedUser = userRepository.save(user);
        return userConverter.toUserResponseDto(savedUser);
    }

    @CacheEvict(value = "users", key = "#email")
    public void updateUserPassword(String token, UserPasswordUpdateDto dto) {
        String email = jwtUtil.extractEmailToken(token.substring(7));

        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new ConflictException("Current password is incorrect.");
        }

        userConverter.updatePassword(user, dto.getNewPassword());
        userRepository.save(user);

        invalidateUserToken(email);
    }

    public void cacheUserToken(String email, String token) {
        String cacheKey = "jwt-token:" + email;
        redisTemplate.opsForValue().set(cacheKey, token, 1, TimeUnit.HOURS);
    }

    public boolean isTokenCached(String email, String token) {
        String cacheKey = "jwt-token:" + email;
        String cachedToken = (String) redisTemplate.opsForValue().get(cacheKey);
        return token.equals(cachedToken);
    }

    public void invalidateUserToken(String email) {
        String cacheKey = "jwt-token:" + email;
        redisTemplate.delete(cacheKey);
    }

    @Cacheable(value = "users", key = "'session:' + #email")
    public UserModel getUserSession(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    @CacheEvict(value = "users", key = "'session:' + #email")
    public void clearUserSession(String email) {
        // Método para limpar cache de sessão quando necessário
    }
}