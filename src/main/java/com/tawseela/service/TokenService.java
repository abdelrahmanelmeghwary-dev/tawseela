package com.tawseela.service;

import com.tawseela.config.TawseelaProperties;
import com.tawseela.dto.response.AuthTokensResponse;
import com.tawseela.entity.RefreshToken;
import com.tawseela.entity.RoleEntity;
import com.tawseela.entity.User;
import com.tawseela.exception.BusinessException;
import com.tawseela.mapper.UserMapper;
import com.tawseela.repository.RefreshTokenRepository;
import com.tawseela.security.JwtService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenService {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TawseelaProperties props;
    private final UserMapper userMapper;

    public TokenService(
            JwtService jwtService,
            RefreshTokenRepository refreshTokenRepository,
            TawseelaProperties props,
            UserMapper userMapper) {
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.props = props;
        this.userMapper = userMapper;
    }

    @Transactional
    public AuthTokensResponse issueForUser(User user) {
        List<String> roleNames =
                user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toList());
        String access = jwtService.createAccessToken(user.getId(), user.getMobileNumber(), roleNames);
        String refreshValue = newRefreshValue();
        persistRefreshToken(user, refreshValue);
        return buildResponse(user, access, refreshValue);
    }

    @Transactional
    public AuthTokensResponse rotateRefresh(String refreshValue) {
        RefreshToken existing = refreshTokenRepository
                .findByTokenAndRevokedIsFalse(refreshValue)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        if (Instant.now().isAfter(existing.getExpiryDate())) {
            existing.setRevoked(true);
            refreshTokenRepository.save(existing);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }
        User user = existing.getUser();
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);
        String newRefresh = newRefreshValue();
        persistRefreshToken(user, newRefresh);
        List<String> roleNames =
                user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toList());
        String access = jwtService.createAccessToken(user.getId(), user.getMobileNumber(), roleNames);
        return buildResponse(user, access, newRefresh);
    }

    @Transactional
    public void revokeRefresh(String refreshValue) {
        refreshTokenRepository
                .findByToken(refreshValue)
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
    }

    @Transactional
    public void revokeRefreshIfOwned(UUID userId, String refreshValue) {
        RefreshToken existing = refreshTokenRepository
                .findByToken(refreshValue)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Refresh token not found"));
        if (!existing.getUser().getId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Refresh token does not belong to current user");
        }
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);
    }

    private void persistRefreshToken(User user, String refreshValue) {
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(refreshValue);
        rt.setExpiryDate(Instant.now().plus(props.getJwt().getRefreshExpirationMs(), ChronoUnit.MILLIS));
        rt.setRevoked(false);
        refreshTokenRepository.save(rt);
    }

    private static String newRefreshValue() {
        byte[] bytes = new byte[48];
        new java.security.SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private AuthTokensResponse buildResponse(User user, String access, String refresh) {
        long expiresInSeconds = props.getJwt().getAccessExpirationMs() / 1000L;
        return new AuthTokensResponse(
                access,
                refresh,
                "Bearer",
                expiresInSeconds,
                userMapper.toAuthMe(user));
    }
}
