package com.parceldelivery.auth.services;

import com.parceldelivery.auth.models.User;
import com.parceldelivery.auth.repository.UserRepository;
import com.parceldelivery.shared.security.jwt.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
	private final UserRepository userRepository;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

		List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().getName().toString()));

		return UserDetailsImpl.builder()
					.id(user.getId())
					.email(user.getEmail())
					.username(user.getUsername())
					.password(user.getPassword())
					.authorities(authorities)
				.build();
	}

}