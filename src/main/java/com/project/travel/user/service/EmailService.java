package com.project.travel.user.service;

import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.user.repository.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final JavaMailSender javaMailSender;

    private final static String EMAIL_CODE_PREFIX = "email:code:";
    private final static String VERIFIED_PREFIX = "email:verified:";

    @Transactional
    public void sendEmailCode(@Email @NotBlank String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        String code = createCode();
        redisTemplate.opsForValue().set(
                EMAIL_CODE_PREFIX + email,
                code,
                Duration.ofMinutes(5)
        );
        sendEmail(email, code);
    }

    @Transactional
    public void verifyEmail(@Email @NotBlank String email, @NotBlank String code) {
        String savedCode = redisTemplate.opsForValue().get(EMAIL_CODE_PREFIX + email);
        if (savedCode == null) {
            throw new CustomException(ErrorCode.EMAIL_CODE_EXPIRED);
        }
        if (!savedCode.equals(code)) {
            throw new CustomException(ErrorCode.EMAIL_CODE_MISMATCH);
        }
        redisTemplate.delete(EMAIL_CODE_PREFIX + email);
        redisTemplate.opsForValue().set(
                VERIFIED_PREFIX + email,
                "true",
                Duration.ofMinutes(30)
        );
    }

    public void checkVerifiedEmail(String email) {
        String verifiedEmail = redisTemplate.opsForValue().get(VERIFIED_PREFIX + email);
        if (!"true".equals(verifiedEmail)) {
            throw new CustomException(ErrorCode.NOT_VERIFIED_EMAIL);
        }
    }

    public void removeVerifiedEmail(String email) {
        redisTemplate.delete(VERIFIED_PREFIX + email);
    }

    private String createCode() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    private void sendEmail(@Email @NotBlank String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[Travel History]이메일 인증 코드");
        message.setText("인증 코드는 " + code + " 입니다. 서둘러 입력해 주세요.");

        javaMailSender.send(message);
    }
}
