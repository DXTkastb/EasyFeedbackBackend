package com.feedbackBackendApp.dbservice;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.feedbackBackendApp.responsedata.Feedback;
import com.feedbackBackendApp.responsedata.Sentence;
import com.feedbackBackendApp.responsedata.Sentiment;
import com.feedbackBackendApp.responsedata.TextSpan;

public class FeedbackMapper implements RowMapper<Feedback>{

	@Override
	public Feedback mapRow(ResultSet rs, int rowNum) throws SQLException {
//		Sentence sentence =  new Sentence();
//		TextSpan textSpan = new TextSpan();
//		textSpan.setContent(rs.getString(1));
//		Sentiment messageSentiment = new Sentiment();
//		messageSentiment.setScore(rs.getDouble(2));
//		sentence.setText(textSpan);
//		sentence.setSentiment(messageSentiment);
//		return sentence; 
		
		Feedback feedback = new Feedback();
		feedback.setId(rs.getInt(1));
		feedback.setFeedback(rs.getString(2));
		feedback.setTime(rs.getDate(3));
		feedback.setVendorUpi(rs.getString(4));
		
		return feedback;
		
	}

}