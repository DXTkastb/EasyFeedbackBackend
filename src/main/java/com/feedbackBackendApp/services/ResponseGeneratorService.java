package com.feedbackBackendApp.services;

import java.time.Instant;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import com.feedbackBackendApp.data.categoryresponse.Category;
import com.feedbackBackendApp.data.categoryresponse.ClassificationCategory;
import com.feedbackBackendApp.data.entitysentimentresponse.EntitySentimentResponse;
import com.feedbackBackendApp.data.feedbackrequestdata.FeedbackData;
import com.feedbackBackendApp.data.sentimentresponse.SentimentResponse;
import com.feedbackBackendApp.responsedata.FinalFeedBack;
import com.feedbackBackendApp.responsedata.FinalFeedBackData;

@Service
public class ResponseGeneratorService {

	@Autowired
	RestTemplate rest;
	@Autowired
	CategoryClassificationService categoryService;
	@Value("${gcp.natural.language.api.key}")
	String gcpKey;

	@Autowired
	DataMapService dataMapService;

	public ResponseEntity<SentimentResponse> getSentimentResponse(HttpEntity<FeedbackData> httpEntity) {
		ResponseEntity<SentimentResponse> responseEntity = rest.exchange(
				"https://language.googleapis.com/v1/documents:analyzeSentiment?key="+gcpKey,
				HttpMethod.POST, httpEntity, SentimentResponse.class);
		return responseEntity;
	}

	public ResponseEntity<Category> getCategory(HttpEntity<FeedbackData> httpEntity) {
		ResponseEntity<Category> responseEntity = rest.exchange(
				"https://language.googleapis.com/v1/documents:classifyText?key="+gcpKey,
				HttpMethod.POST, httpEntity, Category.class);
		return responseEntity;
	}

	public FinalFeedBackData getResponse(String content,int orderNum, int vendorId) {
		
		FinalFeedBackData finalFeedBackData = new FinalFeedBackData();	
		
		finalFeedBackData.setFeedback(content);
		finalFeedBackData.setNum(orderNum);
		finalFeedBackData.setTime((int)(System.currentTimeMillis()%100000000l));
		finalFeedBackData.setVendorID(vendorId);	
		
		HttpEntity<FeedbackData> httpEntity = getHttpEntity(content);
		ResponseEntity<Category> responseCategoryEntity = getCategory(httpEntity);
			
		boolean categoryExists = categoryService.checkCategoryMatch(responseCategoryEntity.getBody().getCategories());
		if(responseCategoryEntity.getStatusCodeValue()==200 &&  categoryExists) {
			finalFeedBackData.setIsInaccurate(0);
			finalFeedBackData.setCategory(getBestCategory(responseCategoryEntity.getBody().getCategories()));
			System.out.println("Feedback +");
		}	
		else {
			finalFeedBackData.setIsInaccurate(1);
			finalFeedBackData.setCategory("/UNKNOWN");
			finalFeedBackData.setSentimentScore(0.0);
			finalFeedBackData.setSentences(null);
			return finalFeedBackData;
		}		
		ResponseEntity<SentimentResponse> responseSentiment = getSentimentResponse(httpEntity);	
		if(responseSentiment.getStatusCodeValue() == 200) 
			{
			System.out.println(responseSentiment.getBody().getDocumentSentiment().getScore());
			finalFeedBackData.setSentimentScore(responseSentiment.getBody().getDocumentSentiment().getScore());
			finalFeedBackData.setSentences(responseSentiment.getBody().getSentences());
		}
		else {
			finalFeedBackData.setSentimentScore(0.0);
			finalFeedBackData.setSentences(null);
		}
		return finalFeedBackData;
	}
	
	public FinalFeedBack getResponse(String content,String vendor_upi_id,
			long user_phone_number) {
	
		FinalFeedBack finalFeedBackData = new FinalFeedBack();	
		finalFeedBackData.setUser_phone_number(user_phone_number);
		finalFeedBackData.setVendorID(vendor_upi_id);
		finalFeedBackData.setFeedback(content);
		finalFeedBackData.setTime(Date.from(Instant.now()));
		
		HttpEntity<FeedbackData> httpEntity = getHttpEntity(content);
		ResponseEntity<Category> responseCategoryEntity = getCategory(httpEntity);
			
		boolean categoryExists = categoryService.checkCategoryMatch(responseCategoryEntity.getBody().getCategories());
		if(responseCategoryEntity.getStatusCodeValue()==200 &&  categoryExists) {
			finalFeedBackData.setIsInaccurate(0);
			finalFeedBackData.setCategory(getBestCategory(responseCategoryEntity.getBody().getCategories()));
			System.out.println("Feedback +");
		}	
		else {
			finalFeedBackData.setIsInaccurate(1);
			finalFeedBackData.setCategory("/UNKNOWN");
			finalFeedBackData.setSentimentScore(0.0);
			finalFeedBackData.setSentences(null);
			return finalFeedBackData;
		}		
		ResponseEntity<SentimentResponse> responseSentiment = getSentimentResponse(httpEntity);	
		if(responseSentiment.getStatusCodeValue() == 200) 
			{
			System.out.println(responseSentiment.getBody().getDocumentSentiment().getScore());
			finalFeedBackData.setSentimentScore(responseSentiment.getBody().getDocumentSentiment().getScore());
			finalFeedBackData.setSentences(responseSentiment.getBody().getSentences());
		}
		else {
			finalFeedBackData.setSentimentScore(0.0);
			finalFeedBackData.setSentences(null);
		}
		return finalFeedBackData;
	}
	
	public ResponseEntity<SentimentResponse> getTest(String content) {
		HttpEntity<FeedbackData> http = getHttpEntity(content);
		return getSentimentResponse(http);		
	}

	public HttpEntity<FeedbackData> getHttpEntity(String content) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		FeedbackData data = new FeedbackData();
		data.setDocument(dataMapService.feedBackDataMap(content));

		HttpEntity<FeedbackData> httpEntity = new HttpEntity<FeedbackData>(data, headers);

		return httpEntity;
	}
	
	
	public String getBestCategory(List<ClassificationCategory> list) {
		String best = list.get(0).getName();
		double max = list.get(0).getConfidence();
		for(ClassificationCategory c:list) {
			
			if(max>=c.getConfidence())
				best = c.getName();
			
		}
		
		return best;
	}

}
