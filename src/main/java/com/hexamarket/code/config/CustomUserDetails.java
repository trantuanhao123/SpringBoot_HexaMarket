package com.hexamarket.code.config;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.hexamarket.code.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CustomUserDetails implements UserDetails {

	private static final long serialVersionUID = 1L;
	private final Long id;
	private final String username;
	private final String password;
	private final Boolean isActive;
	private final Collection<? extends GrantedAuthority> authorities;

	// Factory method dùng Builder của Lombok cho chuyên nghiệp
	public static CustomUserDetails fromUser(User user, Collection<? extends GrantedAuthority> authorities) {

		return CustomUserDetails.builder().id(user.getId()).username(user.getUsername()).password(user.getPassword())
				.isActive(user.getIsActive()).authorities(authorities).build();
	}

	// UserDetails logic

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return Boolean.TRUE.equals(isActive);
	}
}
