package com.parceldelivery.shared.test;

import lombok.experimental.UtilityClass;
import org.hamcrest.Matcher;
import org.springframework.http.HttpStatus;

import static org.hamcrest.Matchers.containsString;

@UtilityClass
public class TestUtil {
	public static Matcher<String> isCode(HttpStatus httpCode) {
		return containsString(String.valueOf(httpCode.value()));
	}
}
