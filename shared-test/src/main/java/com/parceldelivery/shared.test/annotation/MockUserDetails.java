package com.parceldelivery.shared.test.annotation;

import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithUserDetails;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( {ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@WithUserDetails
@WithSecurityContext(factory = CustomWithUserDetailsSecurityContextFactory.class)
public @interface MockUserDetails {
}
