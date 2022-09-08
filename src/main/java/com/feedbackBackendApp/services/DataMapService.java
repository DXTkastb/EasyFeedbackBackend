package com.feedbackBackendApp.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class DataMapService {

	public Map<String, String> feedBackDataMap(String content){
		
		Map<String, String> data = new HashMap<String, String>();
		data.put("content",content);
		data.put("language","en");
		data.put("type", "PLAIN_TEXT");
		
		return data;
	}
	
}
