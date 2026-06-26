package com.project.travel.user.service;

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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Duration;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {
    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("이메일 코드 전송에 성공한다")
    void send_email_code_success() {
//        given
        String email = "user@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());
        when(stringRedisTemplate.opsForValue())
                .thenReturn(valueOperations);

//        when
        emailService.sendEmailCode(email);

//        기존 코드가 변경될 수 있다는 점을 생각해서 다음과 같이 작성한다
//        then
        verify(valueOperations).set(
                startsWith("email:code:"),
                anyString(),
                any(Duration.class)
        );
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("이미 존재하는 이메일이면 예외를 발생한다")
    void send_code_duplicated_email_fail() {
//        given
        String email = "user@test.com";

        User user = User.builder()
                .email(email)
                .password("abcd1234")
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

//        when, then
        assertThatThrownBy(() ->
                emailService.sendEmailCode(email))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.DUPLICATE_EMAIL.getMessage());
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("이메일 코드 인증에 성공한다")
    void verify_email_code_success() {
//        given
        String email = "user@test.com";
        String code = "12345C";

        when(stringRedisTemplate.opsForValue())
                .thenReturn(valueOperations);
        when(valueOperations.get("email:code:" + email))
                .thenReturn(code);

//        when
        emailService.verifyEmail(email, code);

//        기존 코드가 변경될 수 있다는 점을 생각해서 다음과 같이 작성한다
//        then
        verify(stringRedisTemplate).delete(anyString());
        verify(valueOperations).set(
                startsWith("email:verified:"),
                anyString(),
                any(Duration.class)
        );

    }

    @Test
    @DisplayName("이메일 코드가 만료되었으면 예외를 발생한다")
    void verify_expired_email_code_fail() {
//        given
        String email = "user@test.com";
        String code = "12345C";

        when(stringRedisTemplate.opsForValue())
                .thenReturn(valueOperations);
        when(valueOperations.get("email:code:" + email))
                .thenReturn(null);

//        when, then
        assertThatThrownBy(() ->
                emailService.verifyEmail(email, code))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.EMAIL_CODE_EXPIRED.getMessage());
        verify(stringRedisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("이메일 코드가 일치하지 않으면 예외를 발생한다")
    void verify_wrong_email_code_fail() {
//        given
        String email = "user@test.com";
        String code = "12345C";

        when(stringRedisTemplate.opsForValue())
                .thenReturn(valueOperations);
        when(valueOperations.get("email:code:" + email))
                .thenReturn(code);

//        when, then
        assertThatThrownBy(() ->
                emailService.verifyEmail(email, "12345D"))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.EMAIL_CODE_MISMATCH.getMessage());
        verify(stringRedisTemplate, never()).delete(anyString());
    }
}