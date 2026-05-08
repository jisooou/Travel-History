package com.project.travel.user.service;

import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.user.dto.request.UserSignUpRequestDto;
import com.project.travel.user.dto.response.UserSignUpResponseDto;
import com.project.travel.user.entity.User;
import com.project.travel.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailService emailService;

    @Transactional
    public UserSignUpResponseDto signUp(UserSignUpRequestDto requestDto) {
        if (!requestDto.getPassword().equals(requestDto.getConfirmPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }
        emailService.checkVerifiedEmail(requestDto.getEmail());

        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (requestDto.getUserName() != null && userRepository.existsByUserName(requestDto.getUserName())) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXIST);
        }
        String encodedPassword = bCryptPasswordEncoder.encode(requestDto.getPassword());
        User user = User.builder()
                .email(requestDto.getEmail())
                .userName(requestDto.getUserName())
                .password(encodedPassword)
                .build();
        userRepository.save(user);
        emailService.removeVerifiedEmail(requestDto.getEmail());
        return UserSignUpResponseDto.from(user);
    }
}
