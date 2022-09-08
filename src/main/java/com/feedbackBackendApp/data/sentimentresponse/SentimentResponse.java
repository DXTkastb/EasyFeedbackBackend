package com.feedbackBackendApp.data.sentimentresponse;

import java.util.List;

import com.feedbackBackendApp.responsedata.Sentence;
import com.feedbackBackendApp.responsedata.Sentiment;

import lombok.Data;

@Data
public class SentimentResponse {
	
	Sentiment documentSentiment;
	String language;
	List<Sentence> sentences;
}
