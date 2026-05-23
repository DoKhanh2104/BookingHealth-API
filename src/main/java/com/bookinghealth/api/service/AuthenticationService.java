package com.bookinghealth.api.service;

import com.bookinghealth.api.constant.PredefinedRole;
import com.bookinghealth.api.constant.PredefinedStatus;
import com.bookinghealth.api.dto.request.AuthenticationRequest;
import com.bookinghealth.api.dto.request.IntrospectRequest;
import com.bookinghealth.api.dto.request.client.ForgotPasswordRequest;
import com.bookinghealth.api.dto.request.client.GoogleLoginRequest;
import com.bookinghealth.api.dto.request.client.ResetPasswordRequest;
import com.bookinghealth.api.dto.request.client.SignupRequest;
import com.bookinghealth.api.dto.response.AuthenticationResponse;
import com.bookinghealth.api.dto.response.IntrospectResponse;
import com.bookinghealth.api.entity.PasswordReset;
import com.bookinghealth.api.entity.Role;
import com.bookinghealth.api.entity.User;
import com.bookinghealth.api.exception.AppException;
import com.bookinghealth.api.exception.ErrorCode;
import com.bookinghealth.api.repository.PasswordRepository;
import com.bookinghealth.api.repository.RoleRepository;
import com.bookinghealth.api.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.client.RestTemplate;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

  UserRepository userRepository;
      RoleRepository roleRepository;
      RestTemplate restTemplate;
  PasswordRepository tokenRepository;
  JavaMailSender mailSender;

    @NonFinal
  @Value("${jwt.signerKey}")
  protected String SIGNER_KEY;

  @NonFinal
  @Value("${jwt.valid-duration}")
  protected long VALID_DURATION;

  // Tạo token
  private String generateToken(User user) {
    // Header
    JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

    // Dùng email làm subject nếu phone null (đăng nhập Google)
    String subject = (user.getPhone() != null) ? user.getPhone() : user.getEmail();

    JWTClaimsSet jwtClaimsSet =
        new JWTClaimsSet.Builder()
            .subject(subject)
            .issuer("bookinghealth")
            .issueTime(new Date())
            .expirationTime(
                new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
            .jwtID(UUID.randomUUID().toString())
            .claim("scope", buildScope(user))
            .build();

    // Payload
    Payload payload = new Payload(jwtClaimsSet.toJSONObject());

    JWSObject jwsObject = new JWSObject(header, payload);

    // Signer
    try {
      jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
      return jwsObject.serialize();
    } catch (JOSEException e) {
      throw new RuntimeException(e);
    }
  }

  // Build scope (ADMIN, USER, ...)
  private String buildScope(User user) {
    StringJoiner joiner = new StringJoiner(" ");

    if (!CollectionUtils.isEmpty(user.getRoles())) {
      user.getRoles()
          .forEach(
              role -> {
                joiner.add("ROLE_" + role.getRoleName());
              });
    }
    return joiner.toString();
  }

  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
    var user =
        userRepository
            .findByPhone(request.getUsername())
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

    if (!authenticated) {
      throw new AppException(ErrorCode.UNAUTHENTICATED);
    }

    var token = generateToken(user);

    return AuthenticationResponse.builder().token(token).authenticated(true).build();
  }

  public IntrospectResponse introspect(IntrospectRequest request)
      throws JOSEException, ParseException {
    var token = request.getToken();

    JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

    SignedJWT signedJWT = SignedJWT.parse(token);

    Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();

    var verified = signedJWT.verify(verifier);

    return IntrospectResponse.builder().valid(verified && expiration.after(new Date())).build();
  }

  public AuthenticationResponse register(SignupRequest request) {
      if(userRepository.existsByEmail(request.getEmail())){
          throw new AppException(ErrorCode.EMAIL_EXISTED);
      }

      if(userRepository.existsByPhone(request.getPhone())){
          throw new AppException(ErrorCode.PHONE_EXISTED);
      }

      User user = User.builder()
              .email(request.getEmail())
              .phone(request.getPhone())
              .password(new BCryptPasswordEncoder(10).encode(request.getPassword()))
              .name(request.getName())
              .status(PredefinedStatus.ACTIVE)
              .build();
      HashSet<Role> roles = new HashSet<>();
      roleRepository.findByRoleName(PredefinedRole.USER_ROLE).ifPresent(roles::add);
      user.setRoles(roles);

      user.setRoles(user.getRoles());

      userRepository.save(user);

      String token = generateToken(user);

      return AuthenticationResponse.builder().token(token).authenticated(true).build();
  }

  public AuthenticationResponse loginWithGoogle(GoogleLoginRequest request) {
    // 1. Gọi Google UserInfo API để lấy thông tin user
    String googleUserInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(request.getToken());
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    Map<String, Object> googleUser;
    try {
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              googleUserInfoUrl,
              HttpMethod.GET,
              entity,
              new ParameterizedTypeReference<>() {});
      googleUser = response.getBody();
    } catch (Exception e) {
      throw new AppException(ErrorCode.GOOGLE_LOGIN_FAILED);
    }

    if (googleUser == null || googleUser.get("email") == null) {
      throw new AppException(ErrorCode.GOOGLE_LOGIN_FAILED);
    }

    String email = (String) googleUser.get("email");
    String name = (String) googleUser.getOrDefault("name", email);
    String avatar = (String) googleUser.get("picture");

    // 2. Tìm user theo email, nếu chưa có thì tự động tạo mới (auto-register)
    User user =
        userRepository
            .findByEmail(email)
            .orElseGet(
                () -> {
                  User newUser =
                      User.builder()
                          .email(email)
                          .name(name)
                          .avatar(avatar)
                          .status(PredefinedStatus.ACTIVE)
                          .build();
                  HashSet<Role> roles = new HashSet<>();
                  roleRepository.findByRoleName(PredefinedRole.USER_ROLE).ifPresent(roles::add);
                  newUser.setRoles(roles);
                  return userRepository.save(newUser);
                });

    // 3. Cập nhật avatar nếu chưa có
    if (user.getAvatar() == null && avatar != null) {
      user.setAvatar(avatar);
      userRepository.save(user);
    }

    // 4. Tạo JWT của app và trả về
    String token = generateToken(user);
    return AuthenticationResponse.builder().token(token).authenticated(true).build();
  }

  public void forgotPassword(ForgotPasswordRequest request) {
      User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

      String token = UUID.randomUUID().toString();

      LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(10);

      PasswordReset reset = PasswordReset
              .builder()
              .user(user)
              .token(token)
              .expirationTime(expiryDate)
              .build();

      tokenRepository.save(reset);

      // 5. Đóng gói và Gửi Email HTML
      try {
          MimeMessage mimeMessage = mailSender.createMimeMessage();
          MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

          helper.setFrom("cskh.bookinghealth@gmail.com", "BookingHealth");
          helper.setTo(request.getEmail());
          helper.setSubject("[BookingHealth] Yêu cầu đặt lại mật khẩu");

          String htmlContent =
              "<!DOCTYPE html>"
            + "<html lang='vi'>"
            + "<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>"
            + "<style>"
            + "  body { margin:0; padding:0; background:#f0f4f8; font-family: 'Segoe UI', Arial, sans-serif; }"
            + "  .wrapper { background:#f0f4f8; padding:40px 16px; }"
            + "  .card { background:#ffffff; border-radius:16px; max-width:560px; margin:0 auto; overflow:hidden; box-shadow:0 4px 24px rgba(26,113,180,0.10); }"
            + "  .header { background:linear-gradient(135deg,#1a71b4 0%,#0d9488 100%); padding:36px 40px 28px; text-align:center; }"
            + "  .header-logo { font-size:26px; font-weight:800; color:#ffffff; letter-spacing:-0.5px; }"
            + "  .header-logo span { background:rgba(255,255,255,0.2); border-radius:8px; padding:4px 10px; margin-right:8px; font-size:22px; }"
            + "  .header-sub { color:rgba(255,255,255,0.85); font-size:14px; margin-top:6px; }"
            + "  .body { padding:36px 40px; }"
            + "  .greeting { font-size:20px; font-weight:700; color:#1a202c; margin-bottom:12px; }"
            + "  .text { font-size:15px; color:#4a5568; line-height:1.7; margin-bottom:20px; }"
            + "  .token-box { background:#f0f7ff; border:2px dashed #1a71b4; border-radius:12px; padding:20px 24px; text-align:center; margin:24px 0; }"
            + "  .token-label { font-size:12px; font-weight:600; color:#1a71b4; text-transform:uppercase; letter-spacing:1px; margin-bottom:10px; }"
            + "  .token-value { font-size:13px; font-weight:700; color:#1a202c; word-break:break-all; background:#ffffff; border-radius:8px; padding:12px 16px; font-family:monospace; border:1px solid #bee3f8; letter-spacing:0.5px; }"
            + "  .timer { display:inline-block; background:#fff3cd; border-radius:20px; padding:6px 14px; font-size:13px; color:#856404; font-weight:600; margin-bottom:24px; }"
            + "  .divider { height:1px; background:#e2e8f0; margin:24px 0; }"
            + "  .warning { font-size:13px; color:#718096; line-height:1.6; }"
            + "  .footer { background:#f7fafc; padding:20px 40px; text-align:center; }"
            + "  .footer-text { font-size:12px; color:#a0aec0; line-height:1.6; }"
            + "  .footer-brand { font-weight:700; color:#1a71b4; }"
            + "</style></head>"
            + "<body><div class='wrapper'><div class='card'>"
            + "  <div class='header'>"
            + "    <div class='header-logo'><span>🏥</span> BookingHealth</div>"
            + "    <div class='header-sub'>Hệ thống đặt lịch khám sức khoẻ trực tuyến</div>"
            + "  </div>"
            + "  <div class='body'>"
            + "    <div class='greeting'>Xin chào bạn! 👋</div>"
            + "    <p class='text'>Chúng tôi nhận được yêu cầu <strong>đặt lại mật khẩu</strong> cho tài khoản BookingHealth của bạn.<br>Vui lòng sao chép mã xác nhận bên dưới và nhập vào ứng dụng để tiếp tục:</p>"
            + "    <div class='token-box'>"
            + "      <div class='token-label'>🔑 Mã xác nhận đặt lại mật khẩu</div>"
            + "      <div class='token-value'>" + token + "</div>"
            + "    </div>"
            + "    <div style='text-align:center'><span class='timer'>⏱ Mã có hiệu lực trong <strong>10 phút</strong></span></div>"
            + "    <div class='divider'></div>"
            + "    <p class='warning'>⚠️ <strong>Lưu ý bảo mật:</strong> Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này. Mã sẽ tự động hết hạn và tài khoản của bạn vẫn an toàn.</p>"
            + "  </div>"
            + "  <div class='footer'>"
            + "    <p class='footer-text'>Email này được gửi tự động từ <span class='footer-brand'>BookingHealth</span>.<br>Vui lòng không trả lời email này.</p>"
            + "  </div>"
            + "</div></div></body></html>";

          helper.setText(htmlContent, true);
          mailSender.send(mimeMessage);

      } catch (MessagingException e) {
          throw new RuntimeException("Không thể gửi email: " + e.getMessage(), e);
      } catch (UnsupportedEncodingException e) {
          throw new RuntimeException(e);
      }
  }

  public void resetPassword(ResetPasswordRequest request) {
      PasswordReset reset = tokenRepository.findByToken(request.getToken())
              .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

      if(reset.getExpirationTime().isBefore(LocalDateTime.now())) {
          tokenRepository.delete(reset);
          throw new AppException(ErrorCode.TOKEN_EXPIRATION);
      }

      User user = reset.getUser();

      user.setPassword(new BCryptPasswordEncoder(10).encode(request.getNewPassword()));

      userRepository.save(user);

      tokenRepository.delete(reset);
  }

}
