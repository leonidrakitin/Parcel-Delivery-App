package com.parceldelivery.shared.test.annotation;

import com.parceldelivery.shared.security.jwt.UserDetailsImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.util.Assert;

import java.util.List;

public final class CustomWithUserDetailsSecurityContextFactory implements WithSecurityContextFactory<WithUserDetails> {
	@Override
	public SecurityContext createSecurityContext(WithUserDetails userDetails) {
		String username = userDetails.value();
		Assert.hasLength(username, "value() must be non-empty String");
		UserDetails principal = getPrincipal(username);
		Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		return context;
	}

	private static UserDetails getPrincipal(String username) {
		if (username.contains("admin")) {
			return generateAdmin();
		} else if (username.contains("courier")) {
			return generateCourier();
		} else {
			return generateUser();
		}
	}

	private static UserDetailsImpl generateAdmin() {
		return UserDetailsImpl.builder()
					.id(1L)
					.email("admin@admin.com")
					.username("admin")
					.password("p@ssword")
					.authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
				.build();
	}

	private static UserDetailsImpl generateCourier() {
		return UserDetailsImpl.builder()
					.id(3L)
					.email("courier@company.com")
					.username("courier")
					.password("p@ssword")
					.authorities(List.of(new SimpleGrantedAuthority("ROLE_COURIER")))
				.build();
	}

	private static UserDetailsImpl generateUser() {
		return UserDetailsImpl.builder()
					.id(2L)
					.email("user@company.com")
					.username("user")
					.password("p@ssword")
					.authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
				.build();
	}
}