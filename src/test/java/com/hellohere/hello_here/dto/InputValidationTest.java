package com.hellohere.hello_here.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class InputValidationTest {

    private static final ValidatorFactory VALIDATOR_FACTORY =
            Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void registerRejectsInvalidEmailAndPasswordLength() {
        RegisterDto dto = new RegisterDto();
        dto.setEmail("invalid-email");
        dto.setName("user");
        dto.setPassword("1234567");

        Set<ConstraintViolation<RegisterDto>> violations = VALIDATOR.validate(dto);

        assertTrue(hasMessage(violations, "メールアドレスの形式が正しくありません"));
        assertTrue(hasMessage(violations, "パスワードは8文字以上128文字以内で入力してください"));
    }

    @Test
    void registerCountsSupplementaryCharactersAsSingleCharacters() {
        RegisterDto validDto = new RegisterDto();
        validDto.setEmail("test@example.com");
        validDto.setName("😀".repeat(50));
        validDto.setPassword("password");
        RegisterDto invalidDto = new RegisterDto();
        invalidDto.setEmail("test@example.com");
        invalidDto.setName("😀".repeat(51));
        invalidDto.setPassword("password");

        Set<ConstraintViolation<RegisterDto>> validViolations = VALIDATOR.validate(validDto);
        Set<ConstraintViolation<RegisterDto>> invalidViolations = VALIDATOR.validate(invalidDto);

        assertTrue(validViolations.isEmpty());
        assertTrue(hasMessage(invalidViolations, "ユーザー名は50文字以内で入力してください"));
    }

    @Test
    void profileRejectsBlankDisplayNameAndLongBio() {
        ProfileUpdateDto dto = new ProfileUpdateDto();
        dto.setDisplayName(" ");
        dto.setBio("あ".repeat(161));

        Set<ConstraintViolation<ProfileUpdateDto>> violations = VALIDATOR.validate(dto);

        assertTrue(hasMessage(violations, "表示名を入力してください"));
        assertTrue(hasMessage(violations, "自己紹介は160文字以内で入力してください"));
    }

    @Test
    void profileCountsSupplementaryCharactersAsSingleCharacters() {
        ProfileUpdateDto validDto = new ProfileUpdateDto();
        validDto.setDisplayName("😀".repeat(50));
        validDto.setBio("😀".repeat(160));
        ProfileUpdateDto invalidDto = new ProfileUpdateDto();
        invalidDto.setDisplayName("😀".repeat(51));
        invalidDto.setBio("😀".repeat(161));

        Set<ConstraintViolation<ProfileUpdateDto>> validViolations =
                VALIDATOR.validate(validDto);
        Set<ConstraintViolation<ProfileUpdateDto>> invalidViolations =
                VALIDATOR.validate(invalidDto);

        assertTrue(validViolations.isEmpty());
        assertTrue(hasMessage(invalidViolations, "表示名は50文字以内で入力してください"));
        assertTrue(hasMessage(invalidViolations, "自己紹介は160文字以内で入力してください"));
    }

    @Test
    void locationRejectsMissingAndOutOfRangeCoordinates() {
        LocationDto missingLatitude = new LocationDto();
        missingLatitude.setLongitude(BigDecimal.ZERO);
        LocationDto invalidLongitude = new LocationDto();
        invalidLongitude.setLatitude(BigDecimal.ZERO);
        invalidLongitude.setLongitude(new BigDecimal("180.0000001"));

        Set<ConstraintViolation<LocationDto>> missingViolations =
                VALIDATOR.validate(missingLatitude);
        Set<ConstraintViolation<LocationDto>> rangeViolations =
                VALIDATOR.validate(invalidLongitude);

        assertTrue(hasMessage(missingViolations, "緯度を入力してください"));
        assertTrue(hasMessage(rangeViolations, "経度は180以下で入力してください"));
    }

    private boolean hasMessage(Set<? extends ConstraintViolation<?>> violations, String message) {
        return violations.stream().anyMatch(violation -> message.equals(violation.getMessage()));
    }
}
