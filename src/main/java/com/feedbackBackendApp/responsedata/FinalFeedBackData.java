package com.feedbackBackendApp.responsedata;

import java.util.List;

import com.feedbackBackendApp.data.categoryresponse.Category;
import com.feedbackBackendApp.data.entitysentimentresponse.EntitySentimentResponse;
import com.feedbackBackendApp.data.sentimentresponse.SentimentResponse;

import lombok.Data;

@Data
public class FinalFeedBackData {

	int num;
	int time;
	double sentimentScore;
	String category;
	String feedback;
	int vendorID;
	int isInaccurate;
	List<Sentence> sentences;
	List<Sentence> negativeSentences;
	List<Sentence> positiveSentences;
	List<Sentence> neutralSentences;
//	EntitySentimentResponse entitySentimentResponse;
//	Category category;

}
