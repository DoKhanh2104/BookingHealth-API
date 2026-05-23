package com.bookinghealth.api.service;

import com.bookinghealth.api.constant.PredefinedRole;
import com.bookinghealth.api.constant.PredefinedStatus;
import com.bookinghealth.api.dto.request.AuthenticationRequest;
import com.bookinghealth.api.dto.request.IntrospectRequest;
import com.bookinghealth.api.dto.request.client.GoogleLoginRequest;
import com.bookinghealth.api.dto.request.client.SignupRequest;
import com.bookinghealth.api.dto.response.AuthenticationResponse;
import com.bookinghealth.api.dto.response.IntrospectResponse;
import com.bookinghealth.api.entity.Role;
import com.bookinghealth.api.entity.User;
import com.bookinghealth.api.exception.AppException;
import com.bookinghealth.api.exception.ErrorCode;
import com.bookinghealth.api.repository.RoleRepository;
import com.bookinghealth.api.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
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

}
