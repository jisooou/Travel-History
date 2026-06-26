package com.project.travel.user.service;

import com.project.travel.auth.service.TokenRedisService;
import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.user.dto.request.UserSignUpRequestDto;
import com.project.travel.user.dto.response.UserSignUpResponseDto;
import com.project.travel.user.entity.User;
import com.project.travel.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private TokenRedisService tokenRedisService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입에 성공한다")
    void signup_success() {
//        given
        String email = "user@test.com";
        String userName = "user";
        String password = "abcd1234";
        String confirmPassword = "abcd1234";
        String encodedPassword = "encodedPassword";

        UserSignUpRequestDto requestDto = createUserSignUpRequest(email, userName, password, confirmPassword);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());
        when(bCryptPasswordEncoder.encode(password))
                .thenReturn(encodedPassword);

//        when
        UserSignUpResponseDto responseDto = userService.signUp(requestDto);

//        then
        assertThat(responseDto.getEmail()).isEqualTo(email);
        assertThat(responseDto.getUserName()).isEqualTo(userName);

        verify(emailService).checkVerifiedEmail(email);
        verify(userRepository).save(any(User.class));
        verify(emailService).removeVerifiedEmail(email);
    }

    @Test
    @DisplayName("처음에 입력한 비밀번호와 일치하지 않으면 예외가 발생한다")
    void signup_password_mismatch_fail() {
//        given
        String email = "user@test.com";
        String userName = "user";
        String password = "abcd1234";
        String confirmPassword = "abcd123456";

        UserSignUpRequestDto requestDto = createUserSignUpRequest(email, userName, password, confirmPassword);

//        when, then
        assertThatThrownBy(() ->
                userService.signUp(requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.PASSWORD_MISMATCH.getMessage());

        verify(emailService, never()).checkVerifiedEmail(anyString());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입에 사용할 이메일이 중복되면 예외가 발생한다")
    void signup_duplicated_email_fail() {
//        given
        String email = "user@test.com";
        String userName = "user";
        String password = "abcd1234";
        String confirmPassword = "abcd1234";

        UserSignUpRequestDto requestDto = createUserSignUpRequest(email, userName, password, confirmPassword);

        User user = createUser(1, email, userName, "encodedPassword", User.ActiveStatus.ACTIVE);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

//        when, then
        assertThatThrownBy(() ->
                userService.signUp(requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.DUPLICATE_EMAIL.getMessage());

        verify(emailService).checkVerifiedEmail(email);
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).removeVerifiedEmail(anyString());
    }

    @Test
    @DisplayName("회원 탈퇴에 성공한다")
    void signout_success() {
//        given
        Integer userNo = 1;

        User user = createUser(userNo, "user@test.com", "user", "encodedPassword", User.ActiveStatus.ACTIVE);

        when(userRepository.findById(userNo))
                .thenReturn(Optional.of(user));

//        when
        userService.signOut(userNo);

//        then
        assertThat(user.getIsActive()).isEqualTo(User.ActiveStatus.INACTIVE);

        verify(userRepository).findById(userNo);
        verify(tokenRedisService).deleteRefreshToken(userNo);
    }

    @Test
    @DisplayName("이미 탈퇴한 회원이면 예외가 발생한다")
    void signout_fail() {
//        given
        Integer userNo = 1;

        User user = createUser(userNo, "user@test.com", "user", "encodedPassword", User.ActiveStatus.INACTIVE);

        when(userRepository.findById(userNo))
                .thenReturn(Optional.of(user));

//        when, then
        assertThatThrownBy(() ->
                userService.signOut(userNo))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_STATUS_INACTIVE.getMessage());

        verify(tokenRedisService, never()).deleteRefreshToken(anyInt());
    }

    private User createUser(
            Integer userNo,
            String email,
            String userName,
            String password,
            User.ActiveStatus activeStatus
    ) {
        User user = User.builder()
                .email(email)
                .userName(userName)
                .password(password)
                .build();
        ReflectionTestUtils.setField(user, "userNo", userNo);
        ReflectionTestUtils.setField(user, "isActive", activeStatus);
        return user;
    }

    private UserSignUpRequestDto createUserSignUpRequest(
            String email,
            String userName,
            String password,
            String confirmPassword
    ) {
        UserSignUpRequestDto requestDto = new UserSignUpRequestDto();

        ReflectionTestUtils.setField(requestDto, "email", email);
        ReflectionTestUtils.setField(requestDto, "userName", userName);
        ReflectionTestUtils.setField(requestDto, "password", password);
        ReflectionTestUtils.setField(requestDto, "confirmPassword", confirmPassword);
        return requestDto;
    }
}