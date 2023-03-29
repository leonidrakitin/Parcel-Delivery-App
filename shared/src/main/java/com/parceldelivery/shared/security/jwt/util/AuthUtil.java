package com.parceldelivery.shared.security.jwt.util;

import com.parceldelivery.shared.model.RoleType;
import com.parceldelivery.shared.security.jwt.UserDetailsImpl;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class AuthUtil {
	public static UserDetailsImpl getPrincipal() {
		return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	public static RoleType getAuthenticationRole() {
		return RoleType.valueOf(getPrincipal()
				.getAuthorities().iterator().next().getAuthority());
	}

	public static boolean isAdmin() {
		return getAuthenticationRole().equals(RoleType.ROLE_ADMIN);
	}
}
