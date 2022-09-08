package com.feedbackBackendApp.responsedata;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class Entity {
	String name;
	String type;
	Map<String, String> metadata;
	double salience;
	List<EntityMention> mentions;
	Sentiment sentiment;
}
