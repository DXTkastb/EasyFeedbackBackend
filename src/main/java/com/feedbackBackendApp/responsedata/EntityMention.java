package com.feedbackBackendApp.responsedata;

import lombok.Data;

@Data
public class EntityMention {
	TextSpan text;
	String type;
	Sentiment sentiment;
	
}


