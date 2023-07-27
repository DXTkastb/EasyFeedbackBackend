package com.feedbackBackendApp.responsedata;

import lombok.Data;

@Data
public class User {
	String user_name;
	Long user_phone_number;
	String password;
}
