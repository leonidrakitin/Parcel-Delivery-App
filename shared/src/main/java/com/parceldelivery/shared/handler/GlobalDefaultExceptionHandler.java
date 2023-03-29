package com.parceldelivery.shared.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
class GlobalDefaultExceptionHandler {

	@ExceptionHandler(value = Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorResponse defaultErrorHandler(Exception ex) {
		log.error(ex.getClass().getName() + " + " + ex.getMessage());
		return new ErrorResponse(ex.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR.toString());
	}

	@ExceptionHandler(value = ResponseStatusException.class)
	public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
		return ResponseEntity.status(ex.getStatus())
				.body(new ErrorResponse(ex.getReason(), ex.getStatus().toString()));
	}

	@ExceptionHandler(value = AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ErrorResponse handleAccessDeniedException(AccessDeniedException ex) {
		return new ErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN.toString());
	}

	@ExceptionHandler(value = { AuthenticationException.class, AuthorizationServiceException.class })
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ErrorResponse handleAuthenticationException(AuthenticationException ex) {
		return new ErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED.toString());
	}

	@ExceptionHandler(value = ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleConstraintViolationException(ConstraintViolationException ex) {
		return new ErrorResponse(constraintViolationsToString(ex).toString(), HttpStatus.BAD_REQUEST.toString());
	}

	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
		return new ErrorResponse(methodArgumentNotValidtionString(ex).toString(), HttpStatus.BAD_REQUEST.toString());
	}

	private static List<String> constraintViolationsToString(ConstraintViolationException ex) {
		return ex.getConstraintViolations().stream()
				.map(e -> e.getPropertyPath() + "has value '" + e.getInvalidValue() + "'" + e.getMessage())
				.collect(Collectors.toList());
	}

	private static List<String> methodArgumentNotValidtionString(MethodArgumentNotValidException ex) {
		return ex.getBindingResult().getFieldErrors().stream()
				.map(e -> e.getField() + " has value '" + e.getRejectedValue() + "' " + e.getDefaultMessage())
				.collect(Collectors.toList());
	}
}