package com.project.travel.auth.service;

import com.project.travel.auth.dto.request.AuthLoginRequestDto;
import com.project.travel.auth.dto.request.RefreshRequestDto;
import com.project.travel.auth.dto.response.AuthLoginResponseDto;
import com.project.travel.auth.jwt.JwtProvider;
import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.user.entity.User;
import com.project.travel.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TokenRedisService tokenRedisService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("로그인에 성공한다")
    void login_success() {
//        given
        Integer userNo = 1;
        String email = "test@test.com";
        String password = "1234";
        String encodedPassword = "abcd1234";
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        User user = createUser(userNo, email, encodedPassword);
        AuthLoginRequestDto requestDto = createAuthLoginRequest(email, password);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword))
                .thenReturn(true);
        when(jwtProvider.createAccessToken(userNo, email))
                .thenReturn(accessToken);
        when(jwtProvider.createRefreshToken(userNo))
                .thenReturn(refreshToken);
        when(jwtProvider.getRefreshTokenValidityMs())
                .thenReturn(1000L);
        when(jwtProvider.getAccessTokenValidityMs())
                .thenReturn(500L);

//        when
        AuthLoginResponseDto responseDto = authService.login(requestDto);

//        then
        assertThat(responseDto.getAccessToken()).isEqualTo(accessToken);
        assertThat(responseDto.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(responseDto.getTokenType()).isEqualTo("Bearer");
        assertThat(responseDto.getAccessTokenExpire()).isEqualTo(500L);

        verify(tokenRedisService).saveRefreshToken(userNo, refreshToken, 1000L);
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인하면 예외가 발생한다")
    void login_password_fail() {
//        given
        Integer userNo = 1;
        String email = "test@test.com";
        String password = "1234";
        String encodedPassword = "abcd1234";

        User user = createUser(userNo, email, encodedPassword);
        AuthLoginRequestDto requestDto = createAuthLoginRequest(email, password);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword))
                .thenReturn(false);

//        when, then
        assertThatThrownBy(() ->
                authService.login(requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.WRONG_PASSWORD.getMessage());

        verify(jwtProvider, never()).createAccessToken(anyInt(), anyString());
        verify(jwtProvider, never()).createRefreshToken(anyInt());
        verify(tokenRedisService, never()).saveRefreshToken(anyInt(), anyString(), anyLong());
    }

    @Test
    @DisplayName("토큰 재발행에 성공한다")
    void refresh_token_success() {
//        given
        Integer userNo = 1;
        String email = "test@test.com";
        String oldRefreshToken = "old-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        User user = createUser(userNo, email, "abcd1234");
        RefreshRequestDto refreshRequestDto = RefreshRequestDto.builder()
                .refreshToken(oldRefreshToken)
                .build();

        when(jwtProvider.validateToken(oldRefreshToken))
                .thenReturn(true);
        when(jwtProvider.getUserNo(oldRefreshToken))
                .thenReturn(userNo);
        when(tokenRedisService.getRefreshToken(userNo))
                .thenReturn(oldRefreshToken);
        when(userRepository.findById(userNo))
                .thenReturn(Optional.of(user));
        when(jwtProvider.createAccessToken(userNo, email))
                .thenReturn(newAccessToken);
        when(jwtProvider.createRefreshToken(userNo))
                .thenReturn(newRefreshToken);
        when(jwtProvider.getRefreshTokenValidityMs())
                .thenReturn(1000L);
        when(jwtProvider.getAccessTokenValidityMs())
                .thenReturn(500L);

//        when
        AuthLoginResponseDto responseDto = authService.reissue(refreshRequestDto);

//        then
        assertThat(responseDto.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(responseDto.getRefreshToken()).isEqualTo(newRefreshToken);
        assertThat(responseDto.getTokenType()).isEqualTo("Bearer");
        assertThat(responseDto.getAccessTokenExpire()).isEqualTo(500L);

        verify(tokenRedisService).deleteRefreshToken(userNo);
        verify(tokenRedisService).saveRefreshToken(userNo, newRefreshToken, 1000L);
    }

    @Test
    @DisplayName("토큰이 만료되었으면 재발행에 실패하여 예외가 발생한다")
    void refresh_token_expired_fail() {
//        given
        Integer userNo = 1;
        String oldRefreshToken = "old-refresh-token";

        RefreshRequestDto requestDto = RefreshRequestDto.builder()
                .refreshToken(oldRefreshToken)
                .build();

        when(jwtProvider.validateToken(oldRefreshToken))
                .thenReturn(true);
        when(jwtProvider.getUserNo(oldRefreshToken))
                .thenReturn(userNo);
        when(tokenRedisService.getRefreshToken(userNo))
                .thenReturn(null);

//        when, then
        assertThatThrownBy(() ->
                authService.reissue(requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.EXPIRED_REFRESH_TOKEN.getMessage());

        verify(userRepository, never()).findById(anyInt());
        verify(tokenRedisService, never()).deleteRefreshToken(anyInt());
        verify(tokenRedisService, never()).saveRefreshToken(anyInt(), anyString(), anyLong());
    }

    @Test
    @DisplayName("refresh 토큰이 일치하지 않아 예외가 발생한다")
    void refresh_token_mismatch_fail() {
//        given
        Integer userNo = 1;
        String oldeRefreshtoken = "old-refresh-token";
        String savedRefreshtoken = "different-refresh-token";

        RefreshRequestDto requestDto = RefreshRequestDto.builder()
                .refreshToken(oldeRefreshtoken)
                .build();

        when(jwtProvider.validateToken(oldeRefreshtoken))
                .thenReturn(true);
        when(jwtProvider.getUserNo(oldeRefreshtoken))
                .thenReturn(userNo);
        when(tokenRedisService.getRefreshToken(userNo))
                .thenReturn(savedRefreshtoken);

//        when, then
        assertThatThrownBy(() ->
                authService.reissue(requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.DETECTED_DANGER_REFRESH_TOKEN.getMessage());

        verify(tokenRedisService).deleteRefreshToken(userNo);
        verify(userRepository, never()).findById(anyInt());
        verify(tokenRedisService, never()).saveRefreshToken(anyInt(), anyString(), anyLong());
    }

    @Test
    @DisplayName("로그아웃에 성공한다")
    void logout_success() {
//        given
        Integer userNo = 1;
        String accessToken = "access-token";
        Long remainingMs = 1000L;

        when(jwtProvider.validateToken(accessToken))
                .thenReturn(true);
        when(jwtProvider.getUserNo(accessToken))
                .thenReturn(userNo);
        when(jwtProvider.getRemainingExpiration(accessToken))
                .thenReturn(remainingMs);

//        when
        authService.logout(accessToken);

//        then
        verify(tokenRedisService).deleteRefreshToken(userNo);
        verify(tokenRedisService).addToBlacklist(accessToken, remainingMs);
    }

    private User createUser(Integer userNo, String email, String password) {
        User user = User.builder()
                .email(email)
                .userName("test")
                .password(password)
                .build();
        ReflectionTestUtils.setField(user, "userNo", userNo);
        return user;
    }

    private AuthLoginRequestDto createAuthLoginRequest(String email, String password) {
        AuthLoginRequestDto requestDto = new AuthLoginRequestDto();
        ReflectionTestUtils.setField(requestDto, "email", email);
        ReflectionTestUtils.setField(requestDto, "password", password);
        return requestDto;
    }

}