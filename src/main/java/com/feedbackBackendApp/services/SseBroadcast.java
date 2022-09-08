package com.feedbackBackendApp.services;

import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.feedbackBackendApp.responsedata.FinalFeedBackData;

@Service
public class SseBroadcast {

	@Autowired
	DummySessionStore dummy;

	@Async("sseEmitterExecutorService")
	public void broadcastToSession(int id, FinalFeedBackData message) throws InterruptedException {
//		Thread.sleep(5000l);
		if(!(dummy.sseStore.containsKey(id)))
		{
			System.out.println("Session Not Found");
			return;
		}
		
		SseEmitter sessionEmitter = dummy.sseStore.get(id);
		
//		
//		ExecutorService executor = Executors.newSingleThreadExecutor();
//		executor.execute(() -> {

			try {
				sessionEmitter.send((FinalFeedBackData)message,MediaType.APPLICATION_JSON);
				// emitter.complete();

			} catch (Exception e) {
//				sessionEmitter.completeWithError(e);
			}
			
			
//		});
//		executor.shutdown();

	}
	

	
	public void newSseSession(int vectorID,SseEmitter sse) {
		boolean sessionExists = dummy.sseStore.containsKey(vectorID);
//		System.out.println(sessionExists);
		if (sessionExists) {
			SseEmitter oldSse = dummy.sseStore.get(vectorID);
			oldSse.complete();
		}

		dummy.sseStore.put(vectorID, sse);
	}

	
	
}
