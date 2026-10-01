package com.priyex.hrms.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * Annotation to inject the current authenticated UserPrincipal into controller methods.
 * Meta-annotated with @AuthenticationPrincipal so Spring Security resolves it automatically.
 * Usage: public ResponseEntity<?> me(@CurrentUser UserPrincipal principal) { ... }
 */
@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}
