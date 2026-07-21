# Security Configuration Analysis Report
## QuietSpace Backend - Spring Boot Security Assessment

**Date:** July 20, 2026  
**Project:** quietspace-backend  
**Analyzer:** Security Assessment  
**Spring Boot Version:** 4.1.0 (Java 25)  
**Security Framework:** Spring Security 6.x

---

## Executive Summary

| Category | Rating | Status |
|----------|--------|--------|
| **Authentication & JWT** | ⭐⭐⭐⭐☆ (4/5) | Good - Minor improvements needed |
| **Authorization & Access Control** | ⭐⭐⭐☆☆ (3/5) | Needs Improvement - Gaps in method-level security |
| **Web Security Configuration** | ⭐⭐⭐☆☆ (3/5) | Needs Improvement - CSRF disabled, CORS overly permissive |
| **WebSocket Security** | ⭐☆☆☆☆ (1/5) | Critical - No security configuration |
| **Secrets Management** | ⭐⭐⭐☆☆ (3/5) | Needs Improvement - Environment variables but no rotation |
| **Error Handling** | ⭐⭐⭐⭐⭐ (5/5) | Excellent - Consistent, secure error responses |
| **Testing Coverage** | ⭐⭐⭐⭐☆ (4/5) | Good - Unit/integration tests for security components |
| **Overall Security Posture** | ⭐⭐⭐☆☆ (3/5) | **Needs Significant Improvement** |

---

## 1. Architecture Overview

### Security-Related File Structure
```
src/main/java/dev/thural/quietspace/
├── security/
│   ├── SecurityConfig.java           # Main HTTP security config
│   ├── JwtFilter.java                # JWT token validation filter
│   ├── JwtService.java               # JWT token generation/validation
│   ├── JwtAuthEntryPoint.java        # 401 Unauthorized handler
│   ├── CustomAccessDeniedHandler.java # 403 Forbidden handler
│   ├── SecurityErrorHandler.java     # Shared error response formatter
│   ├── Token.java                    # JWT blacklist token entity
│   └── TokenRepository.java          # Token persistence
├── config/
│   ├── AppConfig.java                # AuthManager, UserDetailsService, CORS, PasswordEncoder
│   ├── WebSocketConfig.java          # STOMP/WebSocket config (NO security)
│   └── WebSocketSecurityConfig.java  # EMPTY - placeholder only
├── user/
│   └── User.java                     # Implements UserDetails, Principal
├── auth/
│   └── AuthService.java              # Authentication business logic
└── shared/enums/
    └── Role.java                     # Role/Permission hierarchy
```

### Key Dependencies (build.gradle.kts)
```kotlin
implementation("org.springframework.boot:spring-boot-starter-security")
implementation("org.springframework.security:spring-security-messaging")
implementation("io.jsonwebtoken:jjwt-api:0.13.0")
runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
testImplementation("org.springframework.security:spring-security-test")
```

---

## 2. Detailed Findings

### 2.1 Authentication & JWT Implementation ✅ GOOD

#### Strengths
| Aspect | Implementation |
|--------|----------------|
| **Algorithm** | HS256 (HMAC-SHA256) via `Keys.hmacShaKeyFor()` |
| **Key Management** | Base64-encoded secret from `${JWT_SECRET_KEY}` env var |
| **Token Structure** | Subject=username, claims=authorities+fullName, exp/iat timestamps |
| **Expiration** | Access: 10min (600,000ms), Refresh: 24hr (86,400,000ms) |
| **Blacklisting** | TokenRepository with `existsByToken()` check in JwtFilter |
| **Validation** | Signature verification + expiration + username match |
| **UserDetails** | Custom `User` entity implementing `UserDetails` + `Principal` |

#### Code Reference: `JwtService.java:51-68`
```java
private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
    var authorities = userDetails.getAuthorities()
            .stream().map(GrantedAuthority::getAuthority).toList();
    return Jwts.builder()
            .claims(extraClaims)
            .subject(userDetails.getUsername())
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .claim("authorities", authorities)
            .signWith(getSignInKey())
            .compact();
}
```

#### Issues Found
| Severity | Issue | Location | Recommendation |
|----------|-------|----------|----------------|
| **Medium** | No token rotation on refresh | `AuthService.refreshToken()` | Issue new refresh token, invalidate old |
| **Medium** | Refresh tokens not blacklisted on logout | `AuthService.signout()` | Add refresh token to blacklist |
| **Low** | `SecureRandom` used for activation codes (6-digit numeric) | `AuthService.generateActivationCode()` | Use alphanumeric + longer length |

---

### 2.2 Authorization & Access Control ⚠️ NEEDS IMPROVEMENT

#### Current Configuration: `SecurityConfig.java:30-72`
```java
.authorizeHttpRequests(request -> request
    .requestMatchers("/api/v1/admin/**").hasRole(ADMIN.toString())
    .requestMatchers("/ws", "/ws/**", "/api/v1/ws/**", "/api/v1/auth/**", ...).permitAll()
    .anyRequest().authenticated()
)
```

#### Role/Permission Model: `Role.java`
```java
enum Role {
    USER(Set.of(USER_READ, USER_UPDATE, USER_CREATE, USER_DELETE)),
    ADMIN(Set.of(ADMIN_READ, ADMIN_UPDATE, ADMIN_CREATE, ADMIN_DELETE));

    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = permissions.stream()
            .map(p -> new SimpleGrantedAuthority(p.getPermission()))
            .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + name()));
        return authorities;
    }
}
```

#### Critical Gaps
| Gap | Impact | Location |
|-----|--------|----------|
| **No method-level security enforced** | `@PreAuthorize`/`@Secured` not verified in tests | `SecurityConfig.java:21` enables but no test coverage |
| **No resource-level authorization** | Users can access any post/chat/message | All controllers missing ownership checks |
| **WebSocket endpoints unsecured** | `/ws/**` fully public | `SecurityConfig.java:41-43` + `WebSocketSecurityConfig.java` empty |
| **Admin role only on `/admin/**`** | No granular admin permissions | `SecurityConfig.java:36` |

#### Missing Controller-Level Security
| Controller | Endpoints | Missing Protection |
|------------|-----------|-------------------|
| `PostController` | `/api/v1/posts/**` | Owner-only edit/delete |
| `ChatController` | `/api/v1/chats/**` | Participant-only access |
| `MessageController` | `/api/v1/messages/**` | Chat participant only |
| `CommentController` | `/api/v1/comments/**` | Owner-only edit/delete |
| `ReactionController` | `/api/v1/reactions/**` | Authenticated only (OK) |
| `NotificationController` | `/api/v1/notifications/**` | Recipient-only access |

---

### 2.3 Web Security Configuration ⚠️ NEEDS IMPROVEMENT

#### `SecurityConfig.java` Analysis
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(withDefaults())           // Uses AppConfig.corsFilter()
        .csrf(AbstractHttpConfigurer::disable)  // ❌ CSRF DISABLED
        .authorizeHttpRequests(...)
        .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
        .exceptionHandling(...)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .authenticationProvider(authenticationProvider);
    return http.build();
}
```

| Setting | Current | Recommended | Risk |
|---------|---------|-------------|------|
| **CSRF** | Disabled | Enable for state-changing endpoints | **HIGH** - CSRF vulnerable |
| **CORS** | `allowedOriginPatterns("*")` | Specific origins | **MEDIUM** - Overly permissive |
| **Session** | Stateless (correct) | Stateless | OK |
| **Headers** | Defaults | Explicit security headers | **LOW** - Missing HSTS, CSP, etc. |

#### `AppConfig.java:60-82` - CORS Configuration
```java
@Bean
public CorsFilter corsFilter() {
    final CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.setAllowedOriginPatterns(Collections.singletonList("*"));  // ❌ TOO PERMISSIVE
    config.setAllowedHeaders(Arrays.asList(...));
    config.setAllowedMethods(List.of("GET","POST","DELETE","PUT","PATCH"));
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
}
```

---

### 2.4 WebSocket Security 🔴 CRITICAL

#### Current State: `WebSocketSecurityConfig.java`
```java
@Configuration
public class WebSocketSecurityConfig {
    // EMPTY - NO SECURITY CONFIGURATION
}
```

#### `WebSocketConfig.java` - STOMP Endpoint Configuration
```java
@Override
public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new ChannelInterceptor() {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            // Token validation logic exists but NOT connected to Spring Security
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
            if (StompCommand.CONNECT == accessor.getCommand()) {
                String token = accessor.getFirstNativeHeader("Authorization");
                // ... validates token manually, sets principal
            }
            return message;
        }
    });
}
```

#### Critical Vulnerabilities
| Vulnerability | Impact | Exploitability |
|---------------|--------|----------------|
| **No Spring Security integration** | WebSocket connections bypass HTTP security | **HIGH** - Connect without auth |
| **Manual token parsing** | Error-prone, no filter chain | **MEDIUM** |
| **No message-level authorization** | Any authenticated user can send to any destination | **HIGH** |
| **No `@PreAuthorize` on `@MessageMapping`** | Controller methods unprotected | **HIGH** |

#### Required Fix
```java
// WebSocketSecurityConfig.java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                // Delegate to Spring Security filter chain
                return message;
            }
        });
        // Add Spring Security interceptors
        registration.interceptors(new AuthenticationPrincipalArgumentResolver());
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.addInterceptor(new HttpHandshakeInterceptor());
    }
}
```

---

### 2.5 Error Handling ✅ EXCELLENT

#### `SecurityErrorHandler.java` & Handlers
- **Consistent JSON error format** via `ErrorResponse.of()`
- **No stack traces leaked** - only message + path
- **Proper HTTP status codes**: 401, 403
- **Centralized handling** - both entry point and access denied

```java
// JwtAuthEntryPoint.java:22-38
@Override
public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
    SecurityErrorHandler.handleSecurityError(request, response, authException,
        HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Authentication failed", objectMapper);
}
```

---

### 2.6 Secrets & Configuration Management ⚠️ NEEDS IMPROVEMENT

#### Environment-Based Configuration
| Secret | Source | Rotation Strategy |
|--------|--------|-------------------|
| `JWT_SECRET_KEY` | `${JWT_SECRET_KEY}` env var | **None documented** |
| `DB_USER_PASSWORD` | `${DB_USER_PASSWORD}` | **None documented** |
| `RABBITMQ_PASSWORD` | `${RABBITMQ_PASSWORD}` | **None documented** |
| `MAIL_PASSWORD` | `${MAIL_PASSWORD}` | **None documented** |
| `ADMIN_PASSWORD` | `${ADMIN_PASSWORD}` | **None documented** |
| `EMAIL_API_KEY` | `${EMAIL_API_KEY}` | **None documented** |

#### Configuration Files
- `application.yml` - Base config with env var placeholders ✅
- `application-prod.yml` - Minimal overrides ✅
- `application-dev.yml` - H2 in-memory, Flyway disabled ⚠️ Dev only
- `application-testcontainers.yml` - Test config ✅

#### Missing
- [ ] Secret rotation policy/documentation
- [ ] External secret store (Vault, AWS Secrets Manager, etc.)
- [ ] Secret scanning in CI/CD
- [ ] Distinct secrets per environment (prod uses same pattern)

---

### 2.7 Password Security ✅ GOOD

#### `AppConfig.java:84-87`
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();  // Default strength 10
}
```

- **BCrypt** with default cost factor (10) - appropriate for 2026
- **No plaintext passwords** in code/logs
- **Password never serialized** (`@JsonIgnore` on User.password)

---

### 2.8 Testing Coverage ⭐⭐⭐⭐☆ GOOD

#### Test Files Found
| Test File | Coverage |
|-----------|----------|
| `JwtFilterTest.java` | 6 test cases - filter chain, blacklist, valid/invalid tokens |
| `JwtServiceTest.java` | Token generation, validation, expiration |
| `PostControllerSecurityTest.java` | Controller-level security tests |
| `AuthControllerTest.java` / `AuthControllerSliceTest.java` | Auth endpoint tests |
| `AuthFlowIT.java` | Integration tests |
| `*ControllerSliceTest.java` | 8+ slice tests with mocked UserDetailsService |

#### Missing Test Coverage
- [ ] WebSocket security integration tests
- [ ] Method-level security (`@PreAuthorize`) tests
- [ ] CSRF attack simulation tests
- [ ] CORS misconfiguration tests
- [ ] Token rotation/revocation tests

---

## 3. Compliance & Standards Mapping

### OWASP Top 10 2021 Coverage

| OWASP Category | Status | Notes |
|----------------|--------|-------|
| **A01: Broken Access Control** | ⚠️ Partial | Missing resource-level auth, WebSocket unsecured |
| **A02: Cryptographic Failures** | ✅ Good | BCrypt, HS256, TLS via env config |
| **A03: Injection** | ✅ Good | JPA/Hibernate parameterized queries |
| **A04: Insecure Design** | ⚠️ Partial | No threat model documented |
| **A05: Security Misconfiguration** | ❌ Poor | CSRF disabled, CORS *, empty WS security |
| **A06: Vulnerable Components** | ✅ Good | Spring Boot 4.1.0, jjwt 0.13.0 |
| **A07: Auth Failures** | ✅ Good | JWT stateless, proper 401/403 |
| **A08: Software Integrity** | ⚠️ Unknown | No SBOM/dependency check in build |
| **A09: Logging Failures** | ⚠️ Partial | Structured errors but no security event logging |
| **A10: SSRF** | ✅ Good | No user-controlled URL fetching seen |

### Spring Security Best Practices Compliance

| Practice | Status | Evidence |
|----------|--------|----------|
| Stateless session management | ✅ | `STATELESS` policy |
| Password encoding | ✅ | BCrypt |
| CSRF protection | ❌ | Disabled globally |
| CORS configuration | ⚠️ | Overly permissive |
| Method-level security | ⚠️ | Enabled but unused |
| Security headers | ❌ | Defaults only |
| WebSocket security | ❌ | Empty config |
| Token blacklisting | ✅ | TokenRepository + JwtFilter |
| Secure error handling | ✅ | Custom handlers |

---

## 4. Priority Remediation Plan

### 🔴 CRITICAL (Fix Immediately)

| # | Task | Effort | File(s) |
|---|------|--------|---------|
| 1 | Implement WebSocket security with Spring Security integration | 2-3 days | `WebSocketSecurityConfig.java`, `WebSocketConfig.java` |
| 2 | Enable CSRF protection for state-changing endpoints | 1 day | `SecurityConfig.java` |
| 3 | Add resource-level authorization to all controllers | 3-5 days | All `@Controller` classes |

### 🟠 HIGH (Fix Within Sprint)

| # | Task | Effort | File(s) |
|---|------|--------|---------|
| 4 | Restrict CORS to specific origins | 0.5 day | `AppConfig.java` |
| 5 | Add security headers (HSTS, CSP, X-Frame-Options) | 0.5 day | `SecurityConfig.java` |
| 6 | Implement method-level security (`@PreAuthorize`) | 2 days | Service/Controller layers |
| 7 | Add refresh token rotation on use | 1 day | `AuthService.java`, `TokenRepository.java` |

### 🟡 MEDIUM (Next Sprint)

| # | Task | Effort | File(s) |
|---|------|--------|---------|
| 8 | Document secret rotation procedure | 0.5 day | `docs/` |
| 9 | Add security event logging (login, failed auth, access denied) | 1 day | `JwtAuthEntryPoint`, `CustomAccessDeniedHandler` |
| 10 | Implement dependency vulnerability scanning in CI | 1 day | `build.gradle.kts`, CI config |
| 11 | Add WebSocket message authorization | 1 day | `WebSocketSecurityConfig.java` |

### 🟢 LOW (Technical Debt)

| # | Task | Effort | File(s) |
|---|------|--------|---------|
| 12 | Increase activation code entropy | 0.5 day | `AuthService.java` |
| 13 | Add integration tests for WebSocket security | 1 day | `*WebSocket*IT.java` |
| 14 | Consider asymmetric JWT keys (RS256) for multi-service | 2 days | `JwtService.java`, config |

---

## 5. Code Quality Observations

### Positive Patterns ✅
- **Constructor injection** throughout (`@RequiredArgsConstructor`)
- **Centralized error handling** via `SecurityErrorHandler`
- **Immutable DTOs** with Lombok `@Builder`/`@Value`
- **Interface-based repositories** extending `JpaRepository`
- **Test separation**: Unit (`*Test.java`), Slice (`*SliceTest.java`), Integration (`*IT.java`)

### Areas for Improvement ⚠️
- **Empty configuration class** (`WebSocketSecurityConfig`) suggests incomplete work
- **Commented-out code** in `SecurityConfig.java:38-39` indicates unfinished feature
- **TODO comments** in `User.java:173-175` (photo cleanup) and `AuthService.java:185,193`
- **Hardcoded expiration values** in `application.yml` (should be configurable per env)

---

## 6. Recommended Security Headers Addition

Add to `SecurityConfig.java`:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // ... existing config ...
        .headers(headers -> headers
            .httpStrictTransportSecurity(hsts -> hsts
                .maxAgeInSeconds(31536000)
                .includeSubDomains(true)
                .preload(true)
            )
            .contentSecurityPolicy(csp -> csp
                .policyDirectives("default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'; connect-src 'self'")
            )
            .frameOptions(frame -> frame.deny())
            .contentTypeOptions(withDefaults())
            .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
        );
    return http.build();
}
```

---

## 7. Conclusion

The QuietSpace backend demonstrates **solid foundational security practices** in authentication (JWT), password handling, and error responses. However, **critical gaps exist in WebSocket security, CSRF protection, and resource-level authorization** that must be addressed before production deployment.

**Overall Risk Level: MEDIUM-HIGH** due to WebSocket and CSRF vulnerabilities.

**Recommended Next Steps:**
1. **Week 1**: Implement WebSocket security + Enable CSRF
2. **Week 2**: Add resource-level authorization + Restrict CORS
3. **Week 3**: Method-level security + Security headers + Tests
4. **Ongoing**: Secret rotation policy + Dependency scanning

---

*Report generated by automated security analysis. Manual verification recommended for all findings.*