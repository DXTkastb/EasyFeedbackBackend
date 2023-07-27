package com.feedbackBackendApp.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class DummySessionStore {
	public Map<Integer, SseEmitter> sseStore = new ConcurrentHashMap<Integer, SseEmitter>();
}
