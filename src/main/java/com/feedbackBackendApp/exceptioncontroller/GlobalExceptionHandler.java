package com.feedbackBackendApp.exceptioncontroller;

import java.sql.SQLIntegrityConstraintViolationException;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.feedbackBackendApp.exceptions.UserDoesNotExistException;
import com.feedbackBackendApp.exceptions.VendorDoesNotExistsException;
@CrossOrigin("*")
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
	static final HttpHeaders mainHeaders = new HttpHeaders();
	final static String userExistsError = " User already exits!";
	final static  String userDoesNotExistsError = " Incorrect password or phone number!";
	final static  String vendorDoesNotExistsError = " Incorrect phone number!";
	final static HttpHeaders userExistsHeader = new HttpHeaders();
	final static HttpHeaders unauthorisedUserHeader = new HttpHeaders(); 
	final static HttpHeaders unauthorisedVendorHeader = new HttpHeaders(); 
	
	static {
		userExistsHeader.add("error",userExistsError);
		unauthorisedUserHeader.add("error",userDoesNotExistsError);
		unauthorisedVendorHeader.add("error",vendorDoesNotExistsError);
		mainHeaders.add("Access-Control-Allow-Headers", "*");
		mainHeaders.add("Access-Control-Expose-Headers", "*");
		mainHeaders.add("content-type", " application/json");
		userExistsHeader.addAll(mainHeaders);
		unauthorisedUserHeader.addAll(mainHeaders);
		unauthorisedVendorHeader.addAll(mainHeaders);
	}
	
	@ExceptionHandler(DuplicateKeyException.class)
	public ResponseEntity<Object> handleUserExists1(RuntimeException re, WebRequest request){
		System.out.println("KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK");
		return new ResponseEntity<>(
				userExistsError,
				userExistsHeader,
				HttpStatus.CONFLICT
			);
	}
	
	@ExceptionHandler(SQLIntegrityConstraintViolationException.class)
	public ResponseEntity<Object> handleUserExists2(RuntimeException re, WebRequest request){
		return new ResponseEntity<>(
				userExistsError,
				userExistsHeader,
				HttpStatus.CONFLICT
			);
	}

	@ExceptionHandler(UserDoesNotExistException.class)
	public ResponseEntity<Object> handleUserDoesNotExists(RuntimeException re, WebRequest request){
		System.out.println("ERROR GRABBED!2");
		return new ResponseEntity<>(
				userDoesNotExistsError,
				unauthorisedUserHeader,
				HttpStatus.UNAUTHORIZED
			);
	}
	
	@ExceptionHandler(VendorDoesNotExistsException.class)
	public ResponseEntity<Object> handleVendorDoesNotExists(RuntimeException re, WebRequest request){
		System.out.println("ERROR GRABBED!2");
		return new ResponseEntity<>(
				vendorDoesNotExistsError,
				unauthorisedVendorHeader,
				HttpStatus.UNAUTHORIZED
			);
	}
	// java.sql.SQLIntegrityConstraintViolationException
}
