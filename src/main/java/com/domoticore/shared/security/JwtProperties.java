package com.domoticore.shared.security;

public record JwtProperties(String secret, long expirationMs) {
}
