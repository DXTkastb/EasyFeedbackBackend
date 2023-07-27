package com.feedbackBackendApp.exceptions;

public class VendorDoesNotExistsException extends RuntimeException{
	public VendorDoesNotExistsException(String message) {
		super(message);
	}
}
