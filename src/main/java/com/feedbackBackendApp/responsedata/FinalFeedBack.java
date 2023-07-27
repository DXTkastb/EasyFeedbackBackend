package com.feedbackBackendApp.responsedata;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class FinalFeedBack {
	double sentimentScore;
	long user_phone_number;
	Date time;
	String category;
	String feedback;
	String vendorID;
	int isInaccurate;
	List<Sentence> sentences;
	List<Sentence> negativeSentences;
	List<Sentence> positiveSentences;
	List<Sentence> neutralSentences;
}
