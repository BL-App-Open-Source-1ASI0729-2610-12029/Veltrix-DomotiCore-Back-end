package com.domoticore.shared.infrastructure.security;

public record JwtProperties(String secret, long expirationMs) {
}
