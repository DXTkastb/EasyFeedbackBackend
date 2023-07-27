package com.feedbackBackendApp.responsedata;

import java.sql.Date;
import java.util.List;

import lombok.Data;

@Data
public class FeedbackData {
	int feedback_id;
	Date time;
	double score;
	int inaccurate;
	String feedback;
	List<Sentence> negativeSentences;
	List<Sentence> positiveSentences;
	List<Sentence> neutralSentences;
}
 
