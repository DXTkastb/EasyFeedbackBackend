package com.feedbackBackendApp.exceptions;

public class UserDoesNotExistException extends RuntimeException{
	public UserDoesNotExistException(String message) {
		super(message);
	}
}
