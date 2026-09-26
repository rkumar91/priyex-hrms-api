package com.priyex.hrms.security;

import java.lang.annotation.*;

/**
 * Annotation to inject the current authenticated UserPrincipal into controller methods.
 * Usage: public ResponseEntity<?> me(@CurrentUser UserPrincipal principal) { ... }
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
