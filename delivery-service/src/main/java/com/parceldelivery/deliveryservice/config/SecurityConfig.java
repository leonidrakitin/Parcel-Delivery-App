package com.parceldelivery.deliveryservice.config;

import com.parceldelivery.shared.security.jwt.AuthTokenFilter;
import com.parceldelivery.shared.security.jwt.ConfigProperties;
import com.parceldelivery.shared.security.jwt.CustomAccessDeniedHandler;
import com.parceldelivery.shared.security.jwt.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@ComponentScan("com.parceldelivery.shared")
@EnableGlobalMethodSecurity(jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
	@Bean
	public AuthTokenFilter authenticationJwtTokenFilter(ConfigProperties properties) {
		return new AuthTokenFilter(properties);
	}

	@Bean
	public AccessDeniedHandler accessDeniedHandler() {
		return new CustomAccessDeniedHandler();
	}

	@Bean
	public AuthenticationEntryPoint authenticationEntryPoint() {
		return new CustomAuthenticationEntryPoint();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, ConfigProperties properties)
			throws Exception {
		return http
				.cors().disable()
				.csrf().disable()
				.exceptionHandling()
					.authenticationEntryPoint(authenticationEntryPoint())
					.accessDeniedHandler(accessDeniedHandler())
				.and()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
				.authorizeRequests(auth -> auth
						.antMatchers("/actuator/**", "/v3/api-docs/**").permitAll()
						.anyRequest().authenticated())
				.addFilterBefore(authenticationJwtTokenFilter(properties), UsernamePasswordAuthenticationFilter.class)
				.build();
	}
}
