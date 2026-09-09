package com.pgcompliance.service.impl;

import com.pgcompliance.constant.NotificationEventType;
import com.pgcompliance.constant.NotificationSeverity;
import com.pgcompliance.constant.UserRole;
import com.pgcompliance.dto.ActivationValidationResponseDto;
import com.pgcompliance.dto.CompleteActivationRequestDto;
import com.pgcompliance.entity.AccountActivationToken;
import com.pgcompliance.entity.Room;
import com.pgcompliance.entity.Tenant;
import com.pgcompliance.entity.User;
import com.pgcompliance.exception.ResourceNotFoundException;
import com.pgcompliance.repository.AccountActivationTokenRepository;
import com.pgcompliance.repository.UserRepository;
import com.pgcompliance.service.AccountActivationService;
import com.pgcompliance.service.EmailService;
import com.pgcompliance.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AccountActivationServiceImpl
    implements AccountActivationService {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private static final int TOKEN_VALIDITY_MINUTES = 15;

  private final AccountActivationTokenRepository tokenRepository;

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final EmailService emailService;

  private final NotificationService notificationService;

  @Value("${app.frontend.activation-url:"
      + "http://localhost:5173/activate-account}")
  private String activationPageUrl;

  /**
   * Creates a new activation token and sends
   * the activation email.
   *
   * This method is called after the Admin creates
   * a Tenant portal account.
   */
  @Override
  @Transactional
  public void createAndSendActivation(
      String username) {

    User user = findTenantUser(username);

    if (Boolean.TRUE.equals(
        user.getEmailVerified())) {

      throw new IllegalStateException(
          "Tenant email is already verified");
    }

    issueAndSendActivation(user);
  }

  /**
   * Validates an activation token when the Tenant
   * opens the link received through email.
   */
  @Override
  @Transactional(readOnly = true)
  public ActivationValidationResponseDto validateToken(
      String rawToken) {

    AccountActivationToken activationToken = findByRawToken(rawToken);

    User user = activationToken.getUser();

    Tenant tenant = user.getTenant();

    boolean expired = activationToken.isExpired();

    boolean used = activationToken.isUsed();

    boolean valid = !expired && !used;

    String message;

    if (valid) {

      message = "Activation token is valid";

    } else if (used) {

      message = "Activation token has already been used";

    } else {

      message = "Activation token has expired";
    }

    return ActivationValidationResponseDto
        .builder()
        .valid(valid)
        .expired(expired)
        .used(used)
        .username(
            user.getUsername())
        .tenantName(
            tenant == null
                ? null
                : tenant.getName())
        .message(message)
        .build();
  }

  /**
   * Replaces the unusable placeholder password with
   * the Tenant's private password and activates the
   * portal account.
   */
  @Override
  @Transactional
  public void completeActivation(
      CompleteActivationRequestDto request) {

    validatePasswords(request);

    AccountActivationToken activationToken = findByRawToken(
        request.getToken());

    validateActivationTokenState(
        activationToken);

    User user = activationToken.getUser();

    if (user == null) {

      throw new ResourceNotFoundException(
          "No User account is linked "
              + "to this activation token");
    }

    if (user.getRole() != UserRole.TENANT) {

      throw new IllegalStateException(
          "Only Tenant accounts can be "
              + "activated using this link");
    }

    if (user.getTenant() == null) {

      throw new IllegalStateException(
          "The User account is not linked "
              + "to a Tenant record");
    }

    /*
     * Replace the random placeholder password
     * with the Tenant's private password.
     */
    user.setPassword(
        passwordEncoder.encode(
            request.getNewPassword()));

    user.setEmailVerified(true);

    user.setPasswordChangeRequired(false);

    user.setActive(true);

    /*
     * Flush is important here because the
     * Notification Service checks whether the
     * recipient User is active.
     */
    User activatedUser = userRepository.saveAndFlush(user);

    /*
     * Mark the token as used so it cannot be reused.
     */
    activationToken.setUsedAt(
        LocalDateTime.now());

    tokenRepository.save(
        activationToken);

    /*
     * The User is active now, so in-app
     * notifications can safely be created.
     */
    createActivationNotifications(
        activatedUser);
  }

  /**
   * Creates and sends a fresh activation token.
   *
   * The previous token is deleted and immediately
   * becomes invalid.
   */
  @Override
  @Transactional
  public void resendActivation(
      String username) {

    User user = findTenantUser(username);

    if (Boolean.TRUE.equals(user.getActive())
        && Boolean.TRUE.equals(
            user.getEmailVerified())) {

      throw new IllegalStateException(
          "Account is already active");
    }

    issueAndSendActivation(user);
  }

  /**
   * Creates the Account Activated and Room Assigned
   * in-app notifications after activation succeeds.
   */
  private void createActivationNotifications(
      User activatedUser) {

    Tenant tenant = activatedUser.getTenant();

    if (tenant == null) {
      return;
    }

    notificationService.createNotification(
        activatedUser.getUserId(),

        NotificationEventType.TENANT_ACCOUNT_ACTIVATED,

        NotificationSeverity.INFO,

        "USER",

        "Portal account activated",

        "Your Nandu PG portal account "
            + "has been activated successfully.",

        "/tenant/dashboard",

        "TENANT",

        tenant.getTenantId(),

        "TENANT_ACCOUNT_ACTIVATED:"
            + tenant.getTenantId());

    Room assignedRoom = tenant.getRoom();

    if (assignedRoom == null) {
      return;
    }

    notificationService.createNotification(
        activatedUser.getUserId(),

        NotificationEventType.TENANT_ROOM_ASSIGNED,

        NotificationSeverity.INFO,

        "TENANT",

        "Room assigned",

        "You have been assigned to Room "
            + assignedRoom.getRoomNumber()
            + " in "
            + assignedRoom
                .getBuilding()
                .getBuildingName()
            + ".",

        "/tenant/dashboard",

        "TENANT",

        tenant.getTenantId(),

        "TENANT_ROOM_ASSIGNED:"
            + tenant.getTenantId()
            + ":"
            + assignedRoom.getRoomId());
  }

  /**
   * Invalidates any previous activation token,
   * creates a fresh secure token, stores only its
   * SHA-256 hash, and sends the raw token through
   * the activation email.
   */
  private void issueAndSendActivation(
      User user) {

    /*
     * Only the newest activation link should work.
     */
    tokenRepository.deleteByUserUserId(
        user.getUserId());

    tokenRepository.flush();

    String rawToken = generateRawToken();

    String tokenHash = hashToken(rawToken);

    AccountActivationToken activationToken = AccountActivationToken.builder()
        .user(user)
        .tokenHash(tokenHash)
        .expiresAt(
            LocalDateTime.now()
                .plusMinutes(
                    TOKEN_VALIDITY_MINUTES))
        .createdAt(
            LocalDateTime.now())
        .build();

    tokenRepository.saveAndFlush(
        activationToken);

    Tenant tenant = user.getTenant();

    String activationLink = activationPageUrl
        + "?token="
        + rawToken;

    emailService.sendTenantActivationEmail(
        tenant.getEmail(),
        tenant.getName(),
        user.getUsername(),
        activationLink);
  }

  /**
   * Loads a Tenant User account and validates
   * that an email destination is available.
   */
  private User findTenantUser(
      String username) {

    if (username == null
        || username.isBlank()) {

      throw new IllegalStateException(
          "Username is required");
    }

    String normalizedUsername = username.trim()
        .toLowerCase();

    User user = userRepository
        .findByUsername(
            normalizedUsername)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Tenant account not found"));

    if (user.getRole() != UserRole.TENANT) {

      throw new IllegalStateException(
          "Account is not a Tenant account");
    }

    Tenant tenant = user.getTenant();

    if (tenant == null) {

      throw new IllegalStateException(
          "Account is not linked "
              + "to a Tenant record");
    }

    if (tenant.getEmail() == null
        || tenant.getEmail().isBlank()) {

      throw new IllegalStateException(
          "Tenant email address is unavailable");
    }

    return user;
  }

  /**
   * Finds the activation-token record using the
   * SHA-256 hash of the token received from the
   * frontend.
   */
  private AccountActivationToken findByRawToken(
      String rawToken) {

    if (rawToken == null
        || rawToken.isBlank()) {

      throw new IllegalStateException(
          "Activation token is required");
    }

    String tokenHash = hashToken(
        rawToken.trim());

    return tokenRepository
        .findByTokenHash(tokenHash)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Activation token is invalid"));
  }

  private void validateActivationTokenState(
      AccountActivationToken activationToken) {

    if (activationToken.isUsed()) {

      throw new IllegalStateException(
          "Activation token has already been used");
    }

    if (activationToken.isExpired()) {

      throw new IllegalStateException(
          "Activation token has expired");
    }
  }

  private void validatePasswords(
      CompleteActivationRequestDto request) {

    if (request.getNewPassword() == null
        || request.getNewPassword()
            .isBlank()) {

      throw new IllegalStateException(
          "New password is required");
    }

    if (request.getConfirmPassword() == null
        || request.getConfirmPassword()
            .isBlank()) {

      throw new IllegalStateException(
          "Password confirmation is required");
    }

    if (!request.getNewPassword()
        .equals(
            request.getConfirmPassword())) {

      throw new IllegalStateException(
          "Passwords do not match");
    }

    /*
     * BCrypt accepts a maximum of 72 bytes.
     *
     * Since common ASCII passwords use one byte
     * per character, limiting the password to
     * 72 characters also prevents the BCrypt
     * length exception for the expected input.
     */
    if (request.getNewPassword()
        .length() < 8) {

      throw new IllegalStateException(
          "Password must contain "
              + "at least 8 characters");
    }

    if (request.getNewPassword()
        .getBytes(StandardCharsets.UTF_8).length > 72) {

      throw new IllegalStateException(
          "Password must not exceed "
              + "72 bytes");
    }

    boolean containsUppercase = request.getNewPassword()
        .chars()
        .anyMatch(
            Character::isUpperCase);

    boolean containsLowercase = request.getNewPassword()
        .chars()
        .anyMatch(
            Character::isLowerCase);

    boolean containsNumber = request.getNewPassword()
        .chars()
        .anyMatch(
            Character::isDigit);

    if (!containsUppercase
        || !containsLowercase
        || !containsNumber) {

      throw new IllegalStateException(
          "Password must contain at least "
              + "one uppercase letter, "
              + "one lowercase letter, "
              + "and one number");
    }
  }

  /**
   * Creates a URL-safe, random 256-bit token.
   */
  private String generateRawToken() {

    byte[] randomBytes = new byte[32];

    SECURE_RANDOM.nextBytes(
        randomBytes);

    return Base64
        .getUrlEncoder()
        .withoutPadding()
        .encodeToString(
            randomBytes);
  }

  /**
   * Stores only the token hash in PostgreSQL.
   *
   * If the database is exposed, the raw activation
   * link cannot be reconstructed from the hash.
   */
  private String hashToken(
      String rawToken) {

    try {

      MessageDigest digest = MessageDigest.getInstance(
          "SHA-256");

      byte[] hashedBytes = digest.digest(
          rawToken.getBytes(
              StandardCharsets.UTF_8));

      return HexFormat
          .of()
          .formatHex(hashedBytes);

    } catch (NoSuchAlgorithmException exception) {

      throw new IllegalStateException(
          "Unable to hash activation token",
          exception);
    }
  }
}