package com.feedbackBackendApp.responsedata;

import java.sql.Date;

import lombok.Data;

@Data
public class Feedback {
	int id;
	String feedback;
	Date time;
	String vendorUpi;
}
