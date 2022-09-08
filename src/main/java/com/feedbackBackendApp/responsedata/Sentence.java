package com.feedbackBackendApp.responsedata;

import lombok.Data;

@Data
public class Sentence {

	TextSpan text;
	Sentiment sentiment;
}
