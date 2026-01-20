package com.hexamarket.code.util;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtils {
	@Value("${security.jwt.secret}")
	private String jwtSecret;
	@Value("${security.jwt.access-token-expiration}")
	private long jwtExpiration;
	@Value("${security.jwt.refresh-token-expiration}")
	private long refreshTokenExpiration;

	private SecretKey getSigningKey() {
		byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	// Tạo access token
	public String generateAccessToken(UserDetails userDetails) {
		List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.toList());
		return Jwts.builder().id(UUID.randomUUID().toString())// jti
				.subject(userDetails.getUsername()).claim("roles", roles).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + jwtExpiration))
				.signWith(getSigningKey(), Jwts.SIG.HS256).compact();
	}

	// Tạo refresh token
	public String generateRefreshToken(String username) {
		return Jwts.builder().id(UUID.randomUUID().toString())// jti
				.subject(username).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
				.signWith(getSigningKey(), Jwts.SIG.HS256).compact();

	}

	// PARSE & CLAIMS
	public Claims extractAllClaims(String token) {
		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
	}

	public <T> T extractClaim(String token, Function<Claims, T> resolver) {
		return resolver.apply(extractAllClaims(token));
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public String extractJti(String token) {
		return extractClaim(token, Claims::getId);
	}

	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	@SuppressWarnings("unchecked")
	public List<String> extractRoles(String token) {
		Claims claims = extractAllClaims(token);
		Object roles = claims.get("roles");
		if (roles == null)
			return List.of();
		return (List<String>) roles;
	}

	public List<GrantedAuthority> extractAuthorities(String token) {
		List<String> roles = extractRoles(token);
		return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
	}

	// VALIDATION
	public boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	public void validateToken(String token) {
		try {
			extractAllClaims(token);
		} catch (JwtException e) {
			throw new RuntimeException("JWT signature invalid or expired");
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("JWT token is empty or null");
		}
	}

	// HTTP HELPER
	public String extractTokenFromRequest(HttpServletRequest request) {

		final String header = request.getHeader("Authorization");

		if (header == null || !header.startsWith("Bearer ")) {
			return null;
		}
		return header.substring(7);
	}

	// TTL (for Redis blacklist)
	public long getRemainingTime(String token) {
		Date expiration = extractExpiration(token);
		return expiration.getTime() - System.currentTimeMillis();
	}

	// Thời gian sống của access token (ms)
	public long getAccessTokenExpiration() {
		return jwtExpiration;
	}

	// Thời gian sống của refresh token (ms)
	public long getRefreshTokenExpiration() {
		return refreshTokenExpiration;
	}
}
