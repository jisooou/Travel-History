package com.project.travel.auth.service;

import com.project.travel.auth.dto.request.AuthLoginRequestDto;
import com.project.travel.auth.dto.request.RefreshRequestDto;
import com.project.travel.auth.dto.response.AuthLoginResponseDto;
import com.project.travel.auth.jwt.JwtProvider;
import com.project.travel.auth.jwt.ParsedToken;
import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.user.entity.User;
import com.project.travel.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final TokenRedisService tokenRedisService;

    @Transactional
    public AuthLoginResponseDto login(@Valid AuthLoginRequestDto authLoginRequestDto) {
        User user = userRepository.findByEmail(authLoginRequestDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(authLoginRequestDto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.WRONG_PASSWORD);
        }

        String accessToken = jwtProvider.createAccessToken(user.getUserNo(), user.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(user.getUserNo());

        tokenRedisService.saveRefreshToken(
                user.getUserNo(),
                refreshToken,
                jwtProvider.getRefreshTokenValidityMs()
        );

        return AuthLoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .accessTokenExpire(jwtProvider.getAccessTokenValidityMs())
                .build();
    }

    @Transactional
    public AuthLoginResponseDto reissue(@Valid RefreshRequestDto refreshRequestDto) {
        String oldRefreshToken = refreshRequestDto.getRefreshToken();
        jwtProvider.validateToken(oldRefreshToken);
        Integer userNo = jwtProvider.getUserNo(oldRefreshToken);
        String savedRefreshToken = tokenRedisService.getRefreshToken(userNo);

        if (savedRefreshToken == null) {
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }
        if (!savedRefreshToken.equals(oldRefreshToken)) {
            tokenRedisService.deleteRefreshToken(userNo);
            throw new CustomException(ErrorCode.DETECTED_DANGER_REFRESH_TOKEN);
        }
        User user = userRepository.findByUserNoAndIsActive(userNo, User.ActiveStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        tokenRedisService.deleteRefreshToken(userNo);

        String newAccessToken = jwtProvider.createAccessToken(user.getUserNo(), user.getEmail());
        String newRefreshToken = jwtProvider.createRefreshToken(user.getUserNo());

        tokenRedisService.saveRefreshToken(
                user.getUserNo(),
                newRefreshToken,
                jwtProvider.getRefreshTokenValidityMs()
        );

        return AuthLoginResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .accessTokenExpire(jwtProvider.getAccessTokenValidityMs())
                .build();
    }

    @Transactional
    public void logout(String accessToken) {
        ParsedToken parsedToken = jwtProvider.parsedToken(accessToken);
        tokenRedisService.deleteRefreshToken(parsedToken.userNo());
        tokenRedisService.addToBlacklist(accessToken, parsedToken.remainingExpiration());
    }
}
