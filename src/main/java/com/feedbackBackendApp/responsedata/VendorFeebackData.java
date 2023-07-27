package com.feedbackBackendApp.responsedata;

import java.sql.Date;
import java.util.List;

import lombok.Data;

@Data
public class VendorFeebackData {
	String message;
	double message_score;
	int feedback_id;
	String complete_message;
	Date time;
	double score;
	int inaccurate; 
}
