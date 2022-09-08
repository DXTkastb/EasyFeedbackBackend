package com.feedbackBackendApp.data.entitysentimentresponse;

import java.util.List;

import com.feedbackBackendApp.responsedata.Entity;

import lombok.Data;

@Data
public class EntitySentimentResponse {
	
	List<Entity> entities;
	String language;
	
}
